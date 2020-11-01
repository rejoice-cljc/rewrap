(ns rewrap.impl.parser
  "Parse component arguments.")

(defn- matched-clause?
  "Check whether given `clause` applies to `tag`. 
   Clause can be a predicate fn (e.g. symbol?) or matching value (e.g. :<>)."
  [clause tag]
  (if (fn? clause) (clause tag) (= clause tag)))

(defn create-parsers
  "Merge `custom-parsers` with `final-parser`."
  ([parser] (create-parsers {} parser))
  ([custom-parsers final-parser]
   (merge custom-parsers
        ;; We use custom inline fn as key so as to not override any user defined parsers.
          {#(identity %) final-parser})))

(defn apply-parser 
  "Apply `parser` to `normalized-input`."
  [normalized-input parser custom-parser-fn]
  (if (fn? parser)
    (apply parser normalized-input)
    (custom-parser-fn normalized-input parser)))

(defn parse
  "Parse `input` using `parsers`. 

   Parsers are {clause parser} pairs. 
   A clause is a fn or keyword, matched according to [[matched-clause?]].
   A parser is a fn accepting `normalized-args` or a custom options map for `apply-parser-map`.
   
   Options: 
    - `normalize`, fn, normalizes various input formats
    - `apply-parser-map`, fn, handles parsing when map of options is passed as a parser
    - `terminate-early?`, predicate fn, whether to terminate early from parsing.

   Example parsers for component arguments: 
   e.g. {:<> {:tag 'Fragment}
         keyword? {:props clj->js}}
   Each parser map would be applied by `apply-parser-map`."
  [input {:keys [parsers
                 normalize
                 apply-parser-map
                 terminate-early?]
         :or {parsers {}
              terminate-early? (constantly false)}}]
  (let [initial (normalize input)]
    (reduce (fn [acc [clause parser]]
              (let [tag (first acc)]
                (if (matched-clause? clause tag)
                  (let [parsed-args (apply-parser acc parser apply-parser-map)]
                    (if (terminate-early? parsed-args)
                      (reduced parsed-args)
                      parsed-args))
                  acc)))
            initial
            parsers)))
