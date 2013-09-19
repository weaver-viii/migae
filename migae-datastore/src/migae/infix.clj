;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; File     : infix.clj
;; Function : Infix Math library
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Copyright (c) 2008, J. Bester
;; All rights reserved.
;;
;; Redistribution and use in source and binary forms, with or without
;; modification, are permitted provided that the following conditions are met:
;;     * Redistributions of source code must retain the above copyright
;;       notice, this list of conditions and the following disclaimer.
;;     * Redistributions in binary form must reproduce the above copyright
;;       notice, this list of conditions and the following disclaimer in the
;;       documentation and/or other materials provided with the distribution.
;;
;; THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER ``AS IS'' AND ANY
;; EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
;; WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
;; DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
;; DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
;; (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
;; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
;; ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
;; (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
;; SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; from
;; https://github.com/liebke/incanter/tree/master/modules/incanter-core/src/incanter

(ns ^{:doc "Library for converting infix mathematical formula to prefix expressions"
      :author "J. Bester"}
  migae.infix)


;; operator precedence for formula macro
(def +precedence-table+ (atom {}))

;; symbol translation for symbols in formula (only supports binary operators)
(def +translation-table+ (atom {}))

(def +highest-precedence+ (atom 0))

(defn defop
  "Define operators for formula macro"
  ([op prec & [trans]]
     (swap! +precedence-table+ assoc op prec)
     (when-not (nil? trans)
       (swap! +translation-table+ assoc op trans))
     (reset! +highest-precedence+ (reduce max (map val @+precedence-table+)))))


;; == operators ==
(defop '|| 10 "CompositeFilterOperator.or")
(defop '&& 20 "CompositeFilterOperator.and")
(defop '= 30 "FilterOperator.EQUAL")
(defop '!= 30 "FilterOperator.NOT_EQUAL")
(defop '< 40 "FilterOperator.LESS_THAN")
(defop '> 40 "FilterOperator.GREATER_THAN")
(defop '<= 40 "FilterOperator.LESS_THAN_OR_EQUAL")
(defop '>= 40 "FilterOperator.GREATER_THAN_OR_EQUAL")
(defop '<= 40 "FilterOperator.LESS_THAN_OR_EQUAL")
(defop 'in 40 "FilterOperator.IN")

;; (defop '- 60 '-)
;; (defop '+ 60 '+)
;; (defop '/ 80 '/)
;; (defop '* 80 '*)


(defn- operator?
  "Check if is valid operator"
  ([sym]
     (not (nil? (get @+precedence-table+ sym)))))

(defn- find-lowest-precedence
  "find the operator with lowest precedence; search from left to right"
  ([col]
     ;; loop through terms in the coluence
     (loop [idx 0
	    col col
	    lowest-idx nil
	    lowest-prec @+highest-precedence+]
       ;; nothing left to process
       (if (empty? col)
	 ;; return lowest found
	 lowest-idx
	 ;; otherwise check if current term is lower
	 (let [prec (get @+precedence-table+ (first col))]
	   ;; is of lower or equal precedence
	   (if (and prec (<= prec lowest-prec))
	     (recur (inc idx) (rest col)
		    idx prec)
	     ;; is of high precedence therefore skip for now
	     (recur (inc idx) (rest col)
		    lowest-idx lowest-prec)))))))

(defn- translate-op
  "Translation of symbol => symbol for binary op allows for
user defined operators"
  ([op]
     (get @+translation-table+ op op)))

(defn infix-to-prefix
  "Convert from infix notation to prefix notation"
  ([col]
     (cond
      ;; handle term only
      (not (seq? col)) col
      ;; handle sequence containing one term (i.e. handle parens)
      (= (count col) 1) (infix-to-prefix (first col))
      ;; handle all other cases
      true (let [lowest (find-lowest-precedence col)]
	     (if (nil? lowest) ;; nothing to split
	       col
	       ;; (a b c) bind a to hd, c to tl, and b to op
	       (let [[hd [op & tl]] (split-at lowest col)]
                 ;; (println "hd: " hd)
                 ;; (println "op: " op)
                 ;; (println (format "tl: %s\n" tl))
		 ;; recurse
		 (vector ;; list
		       (infix-to-prefix hd)
                       (translate-op op)
		       (infix-to-prefix tl))))))))

(defmacro $=
  "Formula macro translates from infix to gae query syntax"
  ([& equation]
;     (do (println "form: " equation)
     (vec (infix-to-prefix equation))))

(defmacro filter
  "Formula macro translates from infix to gae query syntax"
  ([& equation]
     (vec (infix-to-prefix equation))))




