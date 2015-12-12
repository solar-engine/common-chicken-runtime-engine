#lang racket
(require scribble/core)
(require scribble/manual)
(require scribble/html-properties)
(require "java-lexer.rkt")
(require parser-tools/lex)

(provide jnew jmethod jmethod* jfield java-format jcode jcode-inline)

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
                (if (equal? (string-ref str 0) #\$)
                    (symboldef (substring str 1))
                    (if (or (and (token? next-tok) (eq? 'IDENTIFIER (token-name next-tok)))
                            (eq? next-tok 'O_PAREN)
                            (eq? last-tok 'new)
                            (and (char-upper-case? (string-ref (symbol->string (token-value tok)) 0))
                                 (or (eq? next-tok 'PERIOD)
                                     (not (equal? (symbol->string (token-value tok)) (string-upcase (symbol->string (token-value tok))))))))
                        (symbol str) ; so we're probably a type!
                        (varsym str))))] ; variable
           [(INTEGER_LIT STRING_LIT FLOAT_LIT) ; DOUBLE_LIT
            (element "RktVal" str)]
           [else (error "oops" tok)]
         ))
        ((symbol? tok)
         (case tok
           [(O_PAREN C_PAREN O_BRACE C_BRACE SEMI_COLON PERIOD <= >= == != = + - * / % && & ^ OR < > ! ? : COMMA AT)
            (element "RktPn" str)]
           [(FALSE_LIT TRUE_LIT)
            (element "RktVal" str)]
           [else (error "oops" tok)]))
        (else (error "oops" tok))))

(define (position-start x)
  (sub1 (position-offset (position-token-start-pos x))))

(define (position-end x)
  (sub1 (position-offset (position-token-end-pos x))))

(define (process-whitespace x)
  (let cont ((remain (string->list x)))
    (cond ((empty? remain) (list))
          ((char-whitespace? (car remain))
           (let-values (((whitespace rest) (splitf-at remain char-whitespace?)))
             (cons (element 'hspace (list->string whitespace)) (cont rest))))
          (else
           (let-values (((printable rest) (splitf-at remain (lambda (x) (not (char-whitespace? x))))))
             (cons (comment (list->string printable)) (cont rest)))))))

(define (java-format-token-with-full full symdef tok tok-last tok-next)
  (let ((since (substring full (position-end tok-last) (position-start tok)))
        (this-section (substring full (position-start tok) (position-end tok))))
    (let ((content (java-format-token (position-token-token tok) (position-token-token tok-last) (position-token-token tok-next) this-section symdef)))
      (append (process-whitespace since) (list content)))))

(define (java-format xes [symdef #f] [sty (style #f '())] [markers empty])
  (define spt (string-split xes "\n"))
  (table
   sty
   (for/list ((line spt)
              (marker (append markers (map (const #f) (range (- (length spt) (length markers)))))))
     (list
      ((if marker (lambda (x) (add-marker x marker)) values)
       (paragraph (style #f '())
                  (java-format-one line symdef)))))))

(define (java-format-one line [symdef #f])
  (let ((lexed (java-lex line)))
    (if (empty? lexed)
        (process-whitespace line)
        (append* (map (curry java-format-token-with-full line symdef)
                      lexed
                      (cons (position-token 'NAH (position 1 #f #f) (position 1 #f #f)) (drop-right lexed 1)) ; last tokens
                      (append (cdr lexed) (list (position-token 'EOF (position 1 #f #f) (position 1 #f #f)))) ; next tokens
                      )))))

(define-syntax-rule (jcode-inline bstr ...)
  (java-format-one (string-append bstr ...)))

(define-syntax jcode
  (syntax-rules ()
    ((jcode #:box marker bstr ...)
     (java-format (string-append bstr ...) #f (style 'boxed '()) (if (list? marker) marker (list marker))))
    ((jcode bstr ...)
     (code-inset (java-format (string-append bstr ...))))))

(define-syntax-rule (jnew name (type arg) ... marker)
  (def-constructor 'name '(type ...) '(arg ...) marker))
(define-syntax jfield
  (syntax-rules (static)
    [(jfield ret static (class field) marker)
     (def-box-p (def-field-i #t 'ret 'class 'field) marker)]
    [(jfield ret (class field) marker)
     (def-box-p (def-field-i #f 'ret 'class 'field) marker)]))
(define-syntax jmethod
  (syntax-rules (static)
    [(jmethod args ... marker)
     (def-box-p (jmethod-parse-i args ...) marker)]))
(define-syntax jmethod-parse-i
  (syntax-rules (static)
    [(jmethod-parse-i ret static (class method) (type arg) ...)
     (def-method-i #t #f 'ret 'class 'method '(type ...) '(arg ...))]
    [(jmethod-parse-i ret static (class method) (type arg) ... #:vararg)
     (def-method-i #t #t 'ret 'class 'method '(type ...) '(arg ...))]
    [(jmethod-parse-i ret (class method) (type arg) ...)
     (def-method-i #f #f 'ret 'class 'method '(type ...) '(arg ...))]
    [(jmethod-parse-i ret (class method) (type arg) ... #:vararg)
     (def-method-i #f #t 'ret 'class 'method '(type ...) '(arg ...))]
    [(jmethod-parse-i ret (method) (type arg) ...)
     (def-method-i #f #f 'ret #f 'method '(type ...) '(arg ...))]
    [(jmethod-parse-i ret (method) (type arg) ... #:vararg)
     (def-method-i #f #t 'ret #f 'method '(type ...) '(arg ...))]))
(define-syntax jmethod*
  (syntax-rules ()
    [(jmethod* (args ...) ... marker)
     (def-box-p* (list (jmethod-parse-i args ...) ...) marker)]))

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
(define colon (element "RktPn" ":"))
(define rpar (element "RktPn" ")"))
(define (symbol x)
  (element "RktSym" (fsym x)))
(define (varsym x)
  (element "RktVar" (fsym x)))
(define (comment x)
  (element "RktCmt" (fsym x)))
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
(define (def-method-i is-static is-vararg ret class method types args)
  (unless (= (length types) (length args))
    (error "types and args length mismatch in def-method-i"))
  (append (if is-static (list (symbol 'static) space) empty)
          (list (symbol ret) space)
          (if class (list (symbol class) dot) empty)
          (list (symboldef method) lpar)
          (append*
           (for/list ((type types) (arg args) (i (length types)))
             (append (if (= i 0)
                         empty
                         (list comma space))
                     (list (symbol (symbol->string type)) space (varsym (symbol->string arg))))))
          (if is-vararg (list (symbol '...)) empty)
          (list rpar)))
(define (def-field-i is-static type class field)
  (append (if is-static (list (symbol 'static) space) empty)
          (list (symbol class) dot (symboldef field) space colon space (symbol type))))
