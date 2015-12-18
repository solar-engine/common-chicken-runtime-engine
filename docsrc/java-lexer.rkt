;; This file comes from https://github.com/jbclements/java
;; It is included in a larger work, which is a LGPL work (see the CCRE root)
;; It is also available under the Mozilla Public License, version 2.0

;; The following was the dependencies listing for the entire previous work:
;; (define deps (list "dherman-struct"
;;                   "io"
;;                   "base"
;;                   "compatibility-lib"
;;                   "parser-tools-lib"
;;                   "srfi-lite-lib"))

;; This was forked because annotations were not supported: specifically, @.

(module lexer mzscheme
  (require (lib "lex.ss" "parser-tools"))
  (require (prefix : (lib "lex-sre.ss" "parser-tools")))
  (require (lib "etc.ss"))

  ;; Lexical Analysis according to the Java Language Specification First Edition 
  ;; chapter 3.
  ;; Lacks all Unicode support

  ;; ===========================================================================
  ;; TOKENS
  ;; ===========================================================================

  (define-empty-tokens Operators
    (PIPE OR OREQUAL
     =	> < !	~	?	:
     ==	<=	>=	!=	&&	++	--
     +	-	*	/	&	^	%	<< >> >>>
     +=	-=	*=	/=	&=	^=	%=	<<=	>>=	>>>=))

  (define-empty-tokens Separators
   (O_PAREN C_PAREN O_BRACE C_BRACE O_BRACKET C_BRACKET SEMI_COLON PERIOD COMMA AT))
  
  (define-empty-tokens EmptyLiterals
   (NULL_LIT TRUE_LIT FALSE_LIT EOF))
  
  (define-empty-tokens Keywords
   (abstract    default    if            private      this
    assert      boolean    do            implements   protected    throw
    break       double     import        public       throws
    byte        else       instanceof    return       transient
    case        extends    int           short        try
    catch       final      interface     static       void
    char        finally    long          strictfp     volatile
    class       float      native        super        while
    const       for        new           switch
    continue    goto       package       synchronized))

  (define-tokens BasicTokens
    (STRING_LIT CHAR_LIT INTEGER_LIT LONG_LIT FLOAT_LIT DOUBLE_LIT 
     IDENTIFIER STRING_ERROR NUMBER_ERROR HEX_LIT OCT_LIT HEXL_LIT OCTL_LIT))

  (define (trim-string s f l)
    (substring s f (- (string-length s) l)))

  ;; ===========================================================================
  ;; REGULAR EXPRESSIONS
  ;; ===========================================================================

  (define-lex-abbrevs
    ;; 3.4
    (CR #\015)
    (LF #\012)
    (LineTerminator (:or CR 
		       LF 
		       (:seq CR LF)))
    (InputCharacter (^ CR LF))
    
    ;; 3.6
    (FF #\014)
    (TAB #\011)
    (WhiteSpace (:or #\space 
		   TAB
		   FF
		   LineTerminator))

    ;; 3.7 (Had to transform CommentTail and CommentTailStar into one RE)
    ;;     (DocumentationComment only appears in version 1 of the spec)
    (Comment (:or TraditionalComment 
		EndOfLineComment
                DocumentationComment))
    (TraditionalComment (:seq "/*" NotStar CommentTail))
    (EndOfLineComment (:seq "//" (:* (:~ CR LF))))
    (DocumentationComment (:seq "/**" CommentTailStar))
    (CommentTail (:seq (:* (:seq (:* NotStar) (:+ "*") NotStarNotSlash))
                    (:* NotStar)
		    (:+ "*")
		    "/"))
    (CommentTailStar (:seq (:* (:seq (:* "*") NotStarNotSlash (:* NotStar) "*"))
                        (:* "*")
                        "/"))
    (NotStar (:or (:~ "*")))
    (NotStarNotSlash (:or (:~ "*" "/")))
                     
    (SyntaxComment (:or TraditionalCommentEOF
                      EndOfLineComment))
    (TraditionalCommentEOF (:seq "/*" CommentTailEOF))
    (CommentTailEOF (:or (:seq (:* (:seq (:* NotStar) (:+ "*") NotStarNotSlash))
                        (:* NotStar)
                        (:+ "*")
                        "/")
                       (:seq (:* (:seq (:* NotStar) (:+ "*") NotStarNotSlash))
                        (:* NotStar)
                        (:* "*"))))

    ;; 3.8 (No need to worry about excluding keywords and such.  They will
    ;;      appear first in the lexer spec)
    (Identifier (:seq JavaLetter (:* JavaLetterOrDigit)))
    (JavaLetter (:or (:/ "A" "Z")
		   (:/ "a" "z")
		   "_"
		   "$"))
    (JavaLetterOrDigit (:or JavaLetter
			  (:/ "0" "9")))

    ;; 3.9
    (Keyword (:or "abstract"  "continue"  "goto"        "package"    "synchronized"
                  "assert"    "default"   "if"          "private"    "this"
                  "boolean"   "do"        "implements"  "protected"  "throw"
                  "break"     "double"    "import"      "public"     "throws"
                  "byte"      "else"      "instanceof"  "return"     "transient"
                  "case"      "extends"   "int"         "short"      "try"
                  "catch"     "final"     "interface"   "static"     "void"
                  "char"      "finally"   "long"        "strictfp"   "volatile"
                  "class"     "float"     "native"      "super"      "while"
                  "const"     "for"       "new"         "switch"))

    ;; 3.10.1
    (Digits (:+ (:/ #\0 #\9)))
    (DigitsOpt (:* (:/ #\0 #\9)))
    
    (IntegerTypeSuffix (:or "l" "L"))
    (DecimalNumeral (:or #\0
		       (:seq (:/ #\1 #\9) (:* (:/ #\0 #\9)))))
    (HexDigit (:or (:/ #\0 #\9)
		 (:/ #\a #\f)
		 (:/ #\A #\F)))
    (HexNumeral (:or (:seq #\0 "x" (:+ HexDigit))
		   (:seq #\0 "X" (:+ HexDigit))))
    (OctalNumeral (:seq #\0 (:+ (:/ #\0 #\7))))
    
    ;; 3.10.2
    
    (FloatTypeSuffix (:or "f" "F"))
    (DoubleTypeSuffix (:or "d" "D"))

    (FloatA (:seq Digits #\. DigitsOpt (:? ExponentPart)))
    (FloatB (:seq #\. Digits (:? ExponentPart)))
    (FloatC (:seq Digits ExponentPart))
    (FloatD (:seq Digits (:? ExponentPart)))
    
    (ExponentPart (:seq (:or "e" "E") (:? (:or "+" "-")) Digits))
    
    ;; MORE

    ;; 3.10.6
    (EscapeSequence (:or "\\b" "\\t" "\\n" "\\f" "\\r" "\\\"" "\\'" "\\\\"
		       (:seq #\\ (:/ #\0 #\3) (:/ #\0 #\7) (:/ #\0 #\7))
		       (:seq #\\ (:/ #\0 #\7) (:/ #\0 #\7))
		       (:seq #\\ (:/ #\0 #\7))))
    
    ;; 3.12
    (Operator (:or "="	">" "<" "!"	"~"	"?"	":"
		 "=="	"<="	">="	"!="	"&&" "||"	"++"	"--"
		 "+"	"-"	"*"	"/"	"&" "|"	"^"	"%"	"<<" ">>" ">>>"
		 "+="	"-="	"*="	"/="	"&="	"|="	"^="	"%="	"<<="	">>="	">>>=")))

  ;; String tokens
  (define-tokens str-tok (STRING_CHAR))
  (define-empty-tokens StringErrors (STRING_END STRING_EOF STRING_NEWLINE))

  (define-struct string-error (string error-token))

  ;; ===========================================================================
  ;; LEXERS
  ;; ===========================================================================

  ;; tokens->string : (listof position-token) -> string
  (define (tokens->string toks)
    (list->string (map (compose token-value position-token-token) toks)))

  ;; string-lexer : position input-port -> position-token
  (define (string-lexer first-token-pos in)
    (let* ([tokens (get-string-tokens in)]
           [rev-tokens (reverse tokens)]
           [last-token (car rev-tokens)]
           [str (tokens->string (reverse (cdr rev-tokens)))])
      (make-position-token
       (if (eq? 'STRING_END (get-token-name last-token))
           (token-STRING_LIT str)
           (token-STRING_ERROR (make-string-error str (position-token-token last-token))))
       first-token-pos
       (position-token-end-pos last-token))))

  ;; get-string-tokens : input-port -> (listof position-token)
  (define (get-string-tokens in)
    (let ((tok (get-string-token in)))
      (case (get-token-name tok)
        ((STRING_EOF STRING_END STRING_NEWLINE) (list tok))
        (else (cons tok (get-string-tokens in))))))

  ;; get-string-token : input-port -> position-token
  (define get-string-token
    (lexer-src-pos
     (#\" (token-STRING_END))
     (EscapeSequence (token-STRING_CHAR (EscapeSequence->char lexeme)))
     ((:~ CR LF) (token-STRING_CHAR (string-ref lexeme 0)))
     ((:or CR LF) (token-STRING_NEWLINE))
     (#\032 (token-STRING_EOF))
     ((eof) (token-STRING_EOF))))

  ;; get-token-name : position-token -> symbol
  (define (get-token-name tok)
    (token-name (position-token-token tok)))

  ;; 3.10.6
  (define (EscapeSequence->char es)
    (cond
     ((string=? es "\\b") #\010)
     ((string=? es "\\t") #\011)
     ((string=? es "\\n") #\012)
     ((string=? es "\\f") #\014)
     ((string=? es "\\r") #\015)
     ((string=? es "\\\"") #\")
     ((string=? es "\\'") #\')
     ((string=? es "\\\\") #\\)
     (else (integer->char (string->number (trim-string es 1 0) 8)))))

  (define java-lexer
    (lexer-src-pos
     ;; 3.12
     (Operator (let ((l lexeme))
                 (cond
                   ((string=? l "|") (token-PIPE))
                   ((string=? l "||") (token-OR))
                   ((string=? l "|=") (token-OREQUAL))
                   (else (string->symbol l)))))
     
     ;; 3.11
     ("(" (token-O_PAREN))
     (")" (token-C_PAREN))
     ("{" (token-O_BRACE))
     ("}" (token-C_BRACE))
     ("[" (token-O_BRACKET))
     ("]" (token-C_BRACKET))
     (";" (token-SEMI_COLON))
     ("," (token-COMMA))
     ("@" (token-AT))
     ("." (token-PERIOD))

     ;; 3.10.7
     ("null" (token-NULL_LIT))

     ;; 3.10.5
     (#\" (return-without-pos (string-lexer start-pos input-port)))

     ;; 3.10.4
     ((:seq #\' (:~ CR LF #\' #\\) #\')
      (token-CHAR_LIT (string-ref lexeme 1)))
     ((:seq #\' EscapeSequence #\') 
      (token-CHAR_LIT (EscapeSequence->char 
                       (trim-string lexeme 1 1))))
     
     ;; 3.10.3
     ("true" (token-TRUE_LIT))
     ("false" (token-FALSE_LIT))

     ;; 3.10.2
     ((:or FloatA FloatB FloatC)
      (token-DOUBLE_LIT (string->number lexeme)))
     ((:seq (:or FloatA FloatB FloatC FloatD) FloatTypeSuffix)
      (token-FLOAT_LIT (string->number (trim-string lexeme 0 1))))
     ((:seq (:or FloatA FloatB FloatC FloatD) DoubleTypeSuffix)
      (token-DOUBLE_LIT (string->number (trim-string lexeme 0 1))))


     ;; 3.10.1
     (DecimalNumeral
      (token-INTEGER_LIT (string->number lexeme 10)))
     ((:seq DecimalNumeral IntegerTypeSuffix)
      (token-LONG_LIT (string->number (trim-string lexeme 0 1) 10)))
     (HexNumeral
      (token-HEX_LIT (string->number (trim-string lexeme 2 0) 16)))
     ((:seq HexNumeral IntegerTypeSuffix)
      (token-HEXL_LIT (string->number (trim-string lexeme 2 1) 16)))
     (OctalNumeral
      (token-OCT_LIT (string->number (trim-string lexeme 1 0) 8)))
     ((:seq OctalNumeral IntegerTypeSuffix)
      (token-OCTL_LIT (string->number (trim-string lexeme 1 1) 8)))

     ;; 3.9
     (Keyword (string->symbol lexeme))

     ;; 3.8
     (Identifier (token-IDENTIFIER (string->symbol lexeme)))

     ;; 3.7
     (Comment (return-without-pos (java-lexer input-port)))

     ;; 3.6
     ((:+ WhiteSpace) (return-without-pos (java-lexer input-port)))

     ;; 3.5
     (#\032 'EOF)
     ((eof) 'EOF)
     
     ((:+ (:or (:/ #\0 #\9)(:/ #\a #\z)(:/ #\A #\Z))) (token-NUMBER_ERROR lexeme))
     
     ))

  (provide (all-defined-except EscapeSequence->char
                               string-lexer get-string-tokens get-string-token get-token-name
                               trim-string)))
