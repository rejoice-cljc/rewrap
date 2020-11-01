(ns rewrap.impl.sexp
  "Transform s-expression output.
   NOTE: This code was forked from: weavejester/hiccup -> r0man/sablono -> rauhs/hicada.")

(defmulti transform-output
  "Transform s-expression `form` output with `transform-fn`.
   Dispatches based on the expr operator's name."
  (fn [form _transform-fn] (name (first form))))

(defmethod transform-output :default [form _] 
  form)

(defmethod transform-output "do"
  [[_ & forms] f]
  `(do ~@(butlast forms) ~(f (last forms))))

(defmethod transform-output "array"
  [[_ & forms] f]
  `(cljs.core/array ~@(mapv f forms)))

(defmethod transform-output "let"
  [[_ bindings & body] f]
  `(let ~bindings ~@(butlast body) ~(f (last body))))

(defmethod transform-output "let*"
  [[_ bindings & body] f]
  `(let* ~bindings ~@(butlast body) ~(f (last body))))

(defmethod transform-output "letfn*"
  [[_ bindings & body] f]
  `(letfn* ~bindings ~@(butlast body) ~(f (last body))))

(defmethod transform-output "for"
  [[_ bindings body] f]
  ;; Rewrite [for] with faster [reduce] alternative that outputs js array.
  (if (== 2 (count bindings))
    (let [[item coll] bindings]
      `(reduce (fn ~'hicada-for-reducer [out-arr# ~item]
                 (.push out-arr# ~(f body))
                 out-arr#)
               (cljs.core/array) ~coll))
    (list 'cljs.core/into-array `(for ~bindings ~(f body)))))

(defmethod transform-output "if"
  [[_ condition a b] f]
  `(if ~condition ~(f a) ~(f b)))

(defmethod transform-output "when"
  [[_ bindings & body] f]
  `(when ~bindings ~@(doall (for [x body] (f x)))))

(defmethod transform-output "when-some"
  [[_ bindings & body] f]
  `(when-some ~bindings ~@(butlast body) ~(f (last body))))

(defmethod transform-output "when-let"
  [[_ bindings & body] f]
  `(when-let ~bindings ~@(butlast body) ~(f (last body))))

(defmethod transform-output "when-first"
  [[_ bindings & body] f]
  `(when-first ~bindings ~@(butlast body) ~(f (last body))))

(defmethod transform-output "when-not"
  [[_ bindings & body] f]
  `(when-not ~bindings ~@(doall (for [x body] (f x)))))

(defmethod transform-output "if-not"
  [[_ bindings & body] f]
  `(if-not ~bindings ~@(doall (for [x body] (f x)))))

(defmethod transform-output "if-some"
  [[_ bindings & body] f]
  `(if-some ~bindings ~@(doall (for [x body] (f x)))))

(defmethod transform-output "if-let"
  [[_ bindings & body] f]
  `(if-let ~bindings ~@(doall (for [x body] (f x)))))

(defmethod transform-output "case"
  [[_ expr & clauses] f]
  `(case ~expr ~@(doall (mapcat
                         (fn [[clause hiccup]]
                           (if hiccup
                             [clause (f hiccup)]
                             [(f test)]))
                         (partition-all 2 clauses)))))

(defmethod transform-output "condp"
  [[_ pred expr & clauses] f]
  `(condp ~pred ~expr ~@(doall (mapcat
                                (fn [[clause hiccup]]
                                  (if hiccup
                                    [clause (f hiccup)]
                                    [(f test)]))
                                (partition-all 2 clauses)))))

(defmethod transform-output "cond"
  [[_ & clauses] f]
  `(cond ~@(doall
            (mapcat
             (fn [[check expr]] [check (f expr)])
             (partition 2 clauses)))))
