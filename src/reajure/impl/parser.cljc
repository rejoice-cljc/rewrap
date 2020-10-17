(ns reajure.impl.parser
  "Parse component arguments.")

(defn- matched-clause?
  "Check whether given `clause` applies to `tag`. 
   Clause can be a predicate fn (e.g. symbol?) or matching value (e.g. :<>)."
  [clause tag]
  (if (fn? clause) (clause tag) (= clause tag)))

(defn parse
  "Compile set of `args` using `parsers`. e.g. 
   Parsers are {clause parser} pairs. 
   A clause is a fn or keyword, matched according to [[matched-clause?]].
   A parser is a fn accepting `normalized-args` or a custom options map for `apply-args-opts`.
   Terminates early is `terminate-early?` returns truthy value.
   
   Example parsers for component arguments: 
   e.g. {:<> {:tag 'Fragment}
         keyword? {:props clj->js}}
   Each parser map would be applied by `apply-args-opts`."
  [args {:keys [parsers
                normalize-args
                apply-args-opts
                terminate-early?]
         :or {parsers {}
              terminate-early? (constantly false)}}]
  (let [nargs (normalize-args args)]
    (reduce (fn [acc [clause parser]]
              (let [tag (first acc)]
                (if (matched-clause? clause tag)
                  (let [parsed-args (if (fn? parser) (apply parser acc) (apply-args-opts acc parser))]
                    (if (terminate-early? parsed-args)
                      (reduced parsed-args)
                      parsed-args))
                  acc)))
            nargs
            parsers)))
