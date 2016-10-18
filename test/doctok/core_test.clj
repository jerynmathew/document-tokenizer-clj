(ns doctok.core-test
  (:require [clojure.test :refer :all]
            [doctok.core :refer :all]))


;; Setup Stop word and documents for testing
(def stop-word-file "/Users/jerynm/workspace/personal/doctok/test/doctok/stop-word-list.txt")
(def document-dresden-wiki "/Users/jerynm/workspace/personal/doctok/test/doctok/test-data-dresdenfiles.txt")
(def top-10-tokens-from-dresden-wiki #{"books" "dresden" "released" "novels" "files" "butcher" "series" "novel" "game" "first"})

(defn same-set= 
    "Compares 2 or more collections and checks if they have same items. Ignores duplicates."
    [& vectors] 
    (apply = (map set vectors)))

(defn contains-many? 
    "Check if given map 'm' has all the given keys 'ks'"
    [m & ks]
    (every? #(contains? m %) ks))

(defn generate-test-doc
    "Generate a test document"
    [file-obj]
    (with-open [file (clojure.java.io/writer file-obj)]
        (binding [*out* file]
            (println "The quick brown fox jumps over a lazy dog!")
            (println "Who packed five dozen old quart jugs in my box.")
            (println "Zelda might fix the job growth plans very quickly on Monday."))))

(deftest test-tokenize-path
    ; Create test document
    (def temp-doc (java.io.File/createTempFile "doc-test-temp" ".txt"))
    (generate-test-doc temp-doc)
    ; Prepare Stop word list
    (def sw-list (generate-stop-word-set stop-word-file))
    (testing "Testing Tockenizer Failed!"
        (is 
            (contains-many? 
                (tokenize sw-list (.getAbsolutePath temp-doc))
                "brown" "fox" "zelda" "growth")))
    ; delete test doc
    (.delete temp-doc))

(deftest negative-test-tokenize-path
    ; Create test document
    (def temp-doc (java.io.File/createTempFile "doc-test-temp" ".txt"))
    (generate-test-doc temp-doc)
    ; Prepare Stop word list
    (def sw-list (generate-stop-word-set stop-word-file))
    (testing "Testing Tockenizer Failed!"
        (is 
            (not (contains-many? 
                            (tokenize sw-list (.getAbsolutePath temp-doc))
                            "mario" "plumber" "lion" "tuesday"))))
    ; delete test doc
    (.delete temp-doc))

(deftest test-compute-1-doc
    (prn "Compute fn w/ 1 document:")
    (def result (time (compute stop-word-file 10 [document-dresden-wiki])))
    (prn "")
    (testing "Compute returned results do not match"
        (is 
            (same-set= 
                result 
                top-10-tokens-from-dresden-wiki))))

(deftest test-compute-2-doc
    (prn "Compute fn w/ 2 document:")
    (def result (time 
                    (compute 
                        stop-word-file 
                        10 
                        (take 2 (repeat document-dresden-wiki)))))
    (prn "")
    (testing "Compute returned results do not match"
        (is 
            (same-set= 
                result 
                top-10-tokens-from-dresden-wiki))))

(deftest test-compute-4-doc
    (prn "Compute fn w/ 4 document:")
    (def result (time 
                    (compute 
                        stop-word-file 
                        10 
                        (take 4 (repeat document-dresden-wiki)))))
    (prn "")
    (testing "Compute returned results do not match"
        (is 
            (same-set= 
                result 
                top-10-tokens-from-dresden-wiki))))

(deftest test-compute-8-doc
    (prn "Compute fn w/ 8 document:")
    (def result (time 
                    (compute 
                        stop-word-file 
                        10 
                        (take 8 (repeat document-dresden-wiki)))))
    (prn "")
    (testing "Compute returned results do not match"
        (is 
            (same-set= 
                result 
                top-10-tokens-from-dresden-wiki))))

(deftest test-compute-10-doc
    (prn "Compute fn w/ 10 document:")
    (def result (time 
                    (compute 
                        stop-word-file 
                        10 
                        (take 10 (repeat document-dresden-wiki)))))
    (prn "")
    (testing "Compute returned results do not match"
        (is 
            (same-set= 
                result 
                top-10-tokens-from-dresden-wiki))))

(deftest test-compute-40-doc
    (prn "Compute fn w/ 40 document:")
    (def result (time 
                    (compute 
                        stop-word-file 
                        10 
                        (take 40 (repeat document-dresden-wiki)))))
    (prn "")
    (testing "Compute returned results do not match"
        (is 
            (same-set= 
                result 
                top-10-tokens-from-dresden-wiki))))

(deftest test-compute-80-doc
    (prn "Compute fn w/ 8 document:")
    (def result (time 
                    (compute 
                        stop-word-file 
                        10 
                        (take 80 (repeat document-dresden-wiki)))))
    (prn "")
    (testing "Compute returned results do not match"
        (is 
            (same-set= 
                result 
                top-10-tokens-from-dresden-wiki))))

