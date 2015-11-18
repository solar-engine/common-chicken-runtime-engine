#lang racket
(require scribble/core)
(require scribble/manual)
(require scribble/html-properties)
(require java/syntax/lexer)
(require parser-tools/lex)

(provide jnew jmethod jmethod* java-format)

(define (java-lex x)
  (define in (open-input-string x))
  (let loop ((so-far empty))
    (let ((next (java-lexer in)))
      (if (eq? 'EOF (token-name (position-token-token next)))
          (reverse so-far)
          (loop (cons next so-far))))))

(define keywords '(abstract
                   default     if         private       this         assert
                   boolean     do         implements    protected    throw
                   break       double     import        public       throws
                   byte        else       instanceof    return       transient
                   case        extends    int           short        try
                   catch       final      interface     static       void
                   char        finally    long          strictfp     volatile
                   class       float      native        super        while
                   const       for        new           switch
                   continue    goto       package       synchronized))

(define (java-format-token tok last-tok next-tok str symdef)
  (cond ((member tok keywords) (keyword str))
        ((token? tok)
         (case (token-name tok)
           [(IDENTIFIER)
            (if (equal? symdef (token-value tok))
                (symboldef str)
                (if (or (and (token? next-tok) (eq? 'IDENTIFIER (token-name next-tok)))
                        (eq? last-tok 'new))
                    (symbol str) ; so we're probably a type!
                    (varsym str)))] ; variable
           [else (error "oops" tok)]
         ))
        ((symbol? tok)
         (case tok
           [(O_PAREN C_PAREN O_BRACE C_BRACE SEMI_COLON)
            (element "RktPn" str)]
           [else (error "oops" tok)]))
        (else (error "oops" tok))))

(define (position-start x)
  (sub1 (position-offset (position-token-start-pos x))))

(define (position-end x)
  (sub1 (position-offset (position-token-end-pos x))))

(define (java-format-token-with-full full symdef tok tok-last tok-next)
;  (println (list tok-last tok-next))
  (let ((since (substring full (position-end tok-last) (position-start tok)))
        (this-section (substring full (position-start tok) (position-end tok))))
    (let ((spacing (element 'hspace since))
          (content (java-format-token (position-token-token tok) (position-token-token tok-last) (position-token-token tok-next) this-section symdef)))
      (if (= 0 (string-length since))
          (list content)
          (list spacing content)))))

(define (java-format x [symdef #f])
  (define lexed (java-lex x))
  (paragraph (style #f '())
             (append* (map (curry java-format-token-with-full x symdef)
                           lexed
                           (cons (position-token 'NAH (position 1 #f #f) (position 1 #f #f)) (drop-right lexed 1)) ; last tokens
                           (append (cdr lexed) (list (position-token 'EOF (position 1 #f #f) (position 1 #f #f)))) ; next tokens
                           ))))
  #|(nested-flow
   (style #f '())
   (map (curry paragraph (style #f (list 'omitable)))
        (append* (map (curry java-format-token-with-full x symdef)
                      lexed
                      (cons (position-token 'NAH (position 1 #f #f) (position 1 #f #f)) (drop-right lexed 1)) ; last tokens
                      (append (cdr lexed) (list (position-token 'EOF (position 1 #f #f) (position 1 #f #f)))) ; next tokens
                      )))))|#

(define-syntax-rule (jnew name (type arg) ... marker)
  (def-constructor 'name '(type ...) '(arg ...) marker))
(define-syntax-rule (jmethod ret (class method) (type arg) ... marker)
  (def-method 'ret 'class 'method '(type ...) '(arg ...) marker))
(define-syntax-rule (jmethod* (ret (class method) (type arg) ...) ... marker)
  (def-box-p* (list (def-method-i 'ret 'class 'method '(type ...) '(arg ...)) ...) marker))

(define (def-box x)
  (def-box* (list x)))
(define (def-box* xes)
  (tabular #:style 'boxed
           (map list xes)))
(define (add-marker x marker)
  (nested-flow
   (style #f '())
   (list
    (nested-flow
     (style "RBackgroundLabel" (list 'decorative 'command (alt-tag "div") (attributes '((class . "SIEHidden")))))
     (list (nested-flow
            (style "RBackgroundLabelInner" (list (alt-tag "div")))
            (list (paragraph (style #f '(omitable)) marker)))))
    x)))
(define (def-box-p elements marker)
  (def-box
    (add-marker (paragraph (style #f (list 'omitable)) elements)
                marker)))
(define (def-box-p* elementses marker)
  (def-box*
    (for/list ((elements elementses))
      (add-marker (paragraph (style #f (list 'omitable)) elements)
                  marker))))
(define (fsym x)
  (if (symbol? x) (symbol->string x) x))
(define (keyword x)
  (element "RktKw" (fsym x)))
(define new (keyword "new"))
(define space (element 'hspace " "))
(define lpar (element "RktPn" "("))
(define comma (element "RktPn" ","))
(define dot (element "RktPn" "."))
(define rpar (element "RktPn" ")"))
(define (symbol x)
  (element "RktSym" (fsym x)))
(define (varsym x)
  (element "RktVar" (fsym x)))
(define (symboldef x)
  (element 'bold (symbol x)))
(define (def-constructor name types args marker)
  (unless (= (length types) (length args))
    (error "types and args length mismatch in def-constructor"))
  (def-box-p (append (list new space (symboldef (symbol->string name)) lpar)
                     (append*
                      (for/list ((type types) (arg args) (i (length types)))
                        (append (if (= i 0)
                                    empty
                                    (list comma space))
                                (list (symbol (symbol->string type)) space (varsym (symbol->string arg))))))
                     (list rpar))
    marker))
(define (def-method-i ret class method types args)
  (unless (= (length types) (length args))
    (error "types and args length mismatch in def-method"))
  (append (list (symbol ret) space (symbol class) dot (symboldef method) lpar)
          (append*
           (for/list ((type types) (arg args) (i (length types)))
             (append (if (= i 0)
                         empty
                         (list comma space))
                     (list (symbol (symbol->string type)) space (varsym (symbol->string arg))))))
          (list rpar)))
(define (def-method ret class method types args marker)
  (unless (= (length types) (length args))
    (error "types and args length mismatch in def-method"))
  (def-box-p (def-method-i ret class method types args) marker))

; @def-code["stuff"]
