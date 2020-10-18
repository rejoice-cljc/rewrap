#!/usr/bin/env bb

(import java.lang.ProcessBuilder$Redirect)

;; https://book.babashka.org/#_java_lang_process
(defn test-runner-watch []
  (let [cmd ["clojure" "-A:kaocha:local:test" "-m" "kaocha.runner" "--watch"] 
        pb (doto (ProcessBuilder. cmd) 
             (.redirectOutput ProcessBuilder$Redirect/INHERIT)) 
        proc (.start pb)] 
    (-> (Runtime/getRuntime)
        (.addShutdownHook (Thread. #(.destroy proc)))) 
    proc))

(.waitFor (test-runner-watch))
