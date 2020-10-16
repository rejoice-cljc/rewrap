(ns reajure.compile.expression
  "NOTE: This code was forked from: weavejester/hiccup -> r0man/sablono -> rauhs/hicada.")

(defmulti parse-output
  "Parse s-expr `form` output data with `emitter` function.
   Dispatches based on the expr operator's name."
  (fn [form _emitter] (name (first form))))

(defmethod parse-output :default [form _] 
  form)

(defmethod parse-output "do"
  [[_ & forms] f]
  `(do ~@(butlast forms) ~(f (last forms))))

(defmethod parse-output "array"
  [[_ & forms] f]
  `(cljs.core/array ~@(mapv f forms)))

(defmethod parse-output "let"
  [[_ bindings & body] f]
  `(let ~bindings ~@(butlast body) ~(f (last body))))

(defmethod parse-output "let*"
  [[_ bindings & body] f]
  `(let* ~bindings ~@(butlast body) ~(f (last body))))

(defmethod parse-output "letfn*"
  [[_ bindings & body] f]
  `(letfn* ~bindings ~@(butlast body) ~(f (last body))))

(defmethod parse-output "for"
  [[_ bindings body] f]
  ;; Rewrite [for] with faster [reduce] alternative that outputs js array.
  (if (== 2 (count bindings))
    (let [[item coll] bindings]
      `(reduce (fn ~'hicada-for-reducer [out-arr# ~item]
                 (.push out-arr# ~(f body))
                 out-arr#)
               (cljs.core/array) ~coll))
    (list 'cljs.core/into-array `(for ~bindings ~(f body)))))

(defmethod parse-output "if"
  [[_ condition a b] f]
  `(if ~condition ~(f a) ~(f b)))

(defmethod parse-output "when"
  [[_ bindings & body] f]
  `(when ~bindings ~@(doall (for [x body] (f x)))))

(defmethod parse-output "when-some"
  [[_ bindings & body] f]
  `(when-some ~bindings ~@(butlast body) ~(f (last body))))

(defmethod parse-output "when-let"
  [[_ bindings & body] f]
  `(when-let ~bindings ~@(butlast body) ~(f (last body))))

(defmethod parse-output "when-first"
  [[_ bindings & body] f]
  `(when-first ~bindings ~@(butlast body) ~(f (last body))))

(defmethod parse-output "when-not"
  [[_ bindings & body] f]
  `(when-not ~bindings ~@(doall (for [x body] (f x)))))

(defmethod parse-output "if-not"
  [[_ bindings & body] f]
  `(if-not ~bindings ~@(doall (for [x body] (f x)))))

(defmethod parse-output "if-some"
  [[_ bindings & body] f]
  `(if-some ~bindings ~@(doall (for [x body] (f x)))))

(defmethod parse-output "if-let"
  [[_ bindings & body] f]
  `(if-let ~bindings ~@(doall (for [x body] (f x)))))

(defmethod parse-output "case"
  [[_ expr & clauses] f]
  `(case ~expr ~@(doall (mapcat
                         (fn [[clause hiccup]]
                           (if hiccup
                             [clause (f hiccup)]
                             [(f test)]))
                         (partition-all 2 clauses)))))

(defmethod parse-output "condp"
  [[_ pred expr & clauses] f]
  `(condp ~pred ~expr ~@(doall (mapcat
                                (fn [[clause hiccup]]
                                  (if hiccup
                                    [clause (f hiccup)]
                                    [(f test)]))
                                (partition-all 2 clauses)))))

(defmethod parse-output "cond"
  [[_ & clauses] f]
  `(cond ~@(doall
            (mapcat
             (fn [[check expr]] [check (f expr)])
             (partition 2 clauses)))))
