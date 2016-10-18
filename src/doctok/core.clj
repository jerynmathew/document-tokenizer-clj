(ns doctok.core
    (:gen-class)
    (:import (java.io BufferedReader FileReader))
    (:require [clojure.string :as string])
    (:require [clojure.java [io :as io]])
    (:require [clojure.tools.cli :refer [parse-opts]])
)


(defn word-tokenizer
    "Generate a new word map based on given map and update given word into it"
    [map-acc word]
    (if (contains? map-acc word)
        (assoc map-acc word (inc (map-acc word)))
        (assoc map-acc word 1)))

(defn line-tokenizer
    "Tokenize the line into a map of words vs its count"
    [stop-word-list acc-map line]
    (as-> line line-arg
        ; Split line by whitespace
        (string/split line-arg #"\s+")
        ; Strip non "\w" characters from start/end of word and extract just theword
        (map (fn [word] (first (re-find #"\w+(.*\w+)*" word))) line-arg)
        ; remove if blank
        (remove string/blank? line-arg)
        ; set word to lowercase
        (map string/lower-case line-arg)
        ; Filter out if word in stop-word-list
        (remove (partial contains? stop-word-list) line-arg)
        ; Append this word to existing map
        (reduce word-tokenizer acc-map line-arg)))

(defn generate-stop-word-set
    "Read the stop word file into a set and return it"
    [stop-word-file]
    (into #{} 
        (line-seq 
            (io/reader stop-word-file))))

(defn tokenize
    "Tokenize given document into a list of words"
    [stop-word-list doc-path]
    ; (println "Path to document:" doc-path)
    (with-open [rdr (BufferedReader. (FileReader. doc-path))]
        ; Using Map because line-seq is lazy evaluated
        (let [results (reduce (partial line-tokenizer stop-word-list) {} (line-seq rdr) )]
            (into (sorted-map-by (fn [key1 key2]
                     (compare [(get results key2) key2]
                              [(get results key1) key1])))
            results))))

(defn compute 
    "Tokenize each document in parallel (through futures ,ie, pmap)
    and join results into a single map and return the top-m frequently occuring tokens"
    [stop-word-file top-m doclist]
    ;; Generate list of stop words
    (def stop-word-list (generate-stop-word-set stop-word-file))
    ;; Now compute top-m frequently occuring tokens in all docs
    (take top-m
        (keys 
            (apply 
                (partial merge-with +) 
                (pmap (partial tokenize stop-word-list) doclist)))))

(defn usage 
    "Return a Usage string to be printed later"
    [options-summary]
    (->> ["The application tokenizes documents and returns top 't' tokens"
          ""
          "Usage: program-name [options] <document-paths>"
          ""
          "Options:"
          options-summary
          ""
          "Arguments:"
          "  List of document paths separated by whitespace"]
          (string/join \newline)))

(defn error-msg 
    "Print 'errors' message"
    [errors]
    (str "The following errors occurred while parsing your command:\n\n"
        (string/join \newline errors)))

(defn exit 
    "Exit application with 'status' code AFTER printing 'msg' on screen"
    [status msg]
    (println msg)
    (System/exit status))

(def cli-options [["-s" "--stop-word" :required "Path to stop word list" 
                                      :parse-fn #(str %)]
                  ["-t" "--top-tokens" :required "How many top frequent tokens to show" 
                                       :parse-fn #(Integer/parseInt %) 
                                       :default 10]
                  ["-h" "--help" ]])


(defn -main
    "Accept stopwords file and documents and process them."
    [& args]
    (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
        ;; Handle help and error conditions
        (cond
            ;; Handle '-h'
            (:help options) (exit 0 (usage summary))
            ;; Handle '-s'
            (empty? (:stop-word options)) (exit 1 (error-msg ["-s/--stop-word option is required"]))
            ;; Handle errors
            errors (exit 1 (error-msg errors))
            ;; Handle args -- At least 1 document to parse
            ;; Add validations for file-check
            (< (count arguments) 1) ((exit 1 (usage summary))))
        
        ;; Trigger application
        (def results (compute (:stop-word options) (:top-tokens options) arguments))

        ;; Terminate agents for handling futures - coz, appln is over
        (shutdown-agents))

    ;; Print Results and exit 0
    (exit 0 results))
