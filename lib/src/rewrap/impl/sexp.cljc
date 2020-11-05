(ns rewrap.impl.sexp
  "Transform s-expression output.
   NOTE: This code was forked from: weavejester/hiccup -> r0man/sablono -> rauhs/hicada.")

(defn- transform-all [f exprs]
  (doall (for [x exprs] (f x))))

(defn- transform-last [f exprs]
  (concat (butlast exprs) [(f (last exprs))]))

(defn- transform-clauses [f clauses]
  (doall (mapcat
          (fn [[check expr]] [check (f expr)])
          (partition-all 2 clauses))))

;; todo: test outputs, dispatch based on full ns
(defmulti transform-output
  "Transform s-expr `forms` output with `transform-fn`.
   Dispatches based on the expr operator's name."
  (fn [forms _transform-fn]
    (name (first forms))))

(defmethod transform-output :default [form _] 
  form)

(defmethod transform-output "console.log"
  [[_ & forms] f]
  `(js/console.log ~@(transform-last f forms)))

(defmethod transform-output "println"
  [[_ & forms] f]
  `(println ~@(transform-last f forms)))

(defmethod transform-output "do"
  [[_ & forms] f]
  `(do ~@(transform-last f forms)))

(defmethod transform-output "array"
  [[_ & forms] f]
  `(cljs.core/array ~@(transform-all f forms)))

(defmethod transform-output "let"
  [[_ bindings & body] f]
  `(let ~bindings ~@(transform-last f body)))

(defmethod transform-output "let*"
  [[_ bindings & body] f]
  `(let* ~bindings ~@(transform-last f body)))

(defmethod transform-output "letfn*"
  [[_ bindings & body] f]
  `(letfn* ~bindings ~@(transform-last f body)))

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
  `(when ~bindings ~@(transform-last body f)))

(defmethod transform-output "when-some"
  [[_ bindings & body] f]
  `(when-some ~bindings ~@(transform-last f body)))

(defmethod transform-output "when-let"
  [[_ bindings & body] f]
  `(when-let ~bindings ~@(transform-last f body)))

(defmethod transform-output "when-first"
  [[_ bindings & body] f]
  `(when-first ~bindings ~@(transform-last f body)))

(defmethod transform-output "when-not"
  [[_ bindings & body] f]
  `(when-not ~bindings ~@(transform-last f body)))

(defmethod transform-output "if-not"
  [[_ bindings & body] f]
  `(if-not ~bindings ~@(transform-all f body)))

(defmethod transform-output "if-some"
  [[_ bindings & body] f]
  `(if-some ~bindings ~@(transform-all f body)))

(defmethod transform-output "if-let"
  [[_ bindings & body] f]
  `(if-let ~bindings ~@(transform-all f body)))

(defmethod transform-output "case"
  [[_ expr & clauses] f]
  `(case ~expr ~@(transform-clauses f clauses)))

(defmethod transform-output "condp"
  [[_ pred expr & clauses] f]
  `(condp ~pred ~expr ~@(transform-clauses f clauses)))

(defmethod transform-output "cond"
  [[_ & clauses] f]
  `(cond ~@(transform-clauses f clauses)))
