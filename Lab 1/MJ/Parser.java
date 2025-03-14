/*  MicroJava Parser (HM 23-03-08)
    ================
*/
package MJ;

import java.util.*;
import MJ.SymTab.*;
import MJ.CodeGen.*;

public class Parser {
	private static final int  // token codes
		none      = 0,
		ident     = 1,
		number    = 2,
		charCon   = 3,
		plus      = 4,
		minus     = 5,
		times     = 6,
		slash     = 7,
		rem       = 8,
		eql       = 9,
		neq       = 10,
		lss       = 11,
		leq       = 12,
		gtr       = 13,
		geq       = 14,
		assign    = 15,
		semicolon = 16,
		comma     = 17,
		period    = 18,
		lpar      = 19,
		rpar      = 20,
		lbrack    = 21,
		rbrack    = 22,
		lbrace    = 23,
		rbrace    = 24,
		class_    = 25,
		else_     = 26,
		final_    = 27,
		if_       = 28,
		new_      = 29,
		print_    = 30,
		program_  = 31,
		read_     = 32,
		return_   = 33,
		void_     = 34,
		while_    = 35,
		eof       = 36;
	private static final String[] name = { // token names for error messages
	"none", "identifier", "number", "char constant", "+", "-", "*", "/", "%",
	"==", "!=", "<", "<=", ">", ">=", "=", ";", ",", ".", "(", ")",
	"[", "]", "{", "}", "class", "else", "final", "if", "new", "print",
	"program", "read", "return", "void", "while", "eof"
	};

	private static Token t;				// current token (recently recognized)
	private static Token la;			// lookahead token
	private static int sym;				// always contains la.kind
	public  static int errors;  	// error counter
	private static int errDist;		// no. of correctly recognized tokens since last error

	private static Obj curMethod;	// currently compiled method

	//----------- terminal first/sync sets; initialized in method parse() -----
	private static BitSet firstExpr, firstStat, syncStat, syncDecl;

	//------------------- auxiliary methods ----------------------
	private static void scan() {
		t = la;
		la = Scanner.next();
		sym = la.kind;
		errDist++;
		/*
		System.out.print("line " + la.line + ", col " + la.col + ": " + name[sym]);
		if (sym == ident) System.out.print(" (" + la.val + ")");
		if (sym == number || sym == charCon) System.out.print(" (" + la.numVal + ")");
		System.out.println();*/
	}

	private static void check(int expected) {
		if (sym == expected) scan();
		else error(name[expected] + " expected");
	}

	public static void error(String msg) { // syntactic error at token la
		if (errDist >= 3) {
			System.out.println("-- line " + la.line + " col " + la.col + ": " + msg);
			errors++;
		}
		errDist = 0;
	}

	//-------------- parsing methods (in alphabetical order) -----------------


	private static void Addop() {
		//Addop = "+" | "-";
		if (sym == plus) {
			check(plus);
		} else if (sym == minus) {
			check(minus);
		} else {
			error("Invalid operator at Addop()");
		}
	}
	
	private static void ActPars(){
		//ActPars = "(" [ Expr {"," Expr} ] ")";
		check(lpar);
		if (sym == Expr()){
			while(true){
				check(comma);
				Expr();
			}
		} else {
			break;
		}
		check(rpar);
	}
		
	private static void Block() {
		//Block = "{" {Statement} "}";
		check(lbrace);
		while(true){
			statement();
		}
		check(rbrace);
	}
		
	private static void ClassDecl() {
		//ClassDecl = "class" ident "{" {VarDecl} "}";
		
		check(class_);
		check(ident);
		check(lbrace);
		while (true) {
			check(VarDecl);
		}
		check(rbrace);
	}

	private static void Condition() {
		//Condition = Expr Relop Expr;
		Expr();
		Relop();
		Expr();
	}
	
	private static void ConstDecl() {
		//ConstDecl = "final" Type ident "=" (number | charConst) ";";
		check(final_);
		Type();
		check(ident);
		check(assign);
		if (sym == number) {
			check(number);
		} else if (sym == charConst){
			check(charConst);
		} else {
			error("Invalid symbol at ConstDecl");
		}
		check(semicolon);
	}
	
	private static void Designator() {
		//Designator = ident {"." ident | "[" Expr "]"};
		check(ident);
		while (true) {
			if (sym == ident) {
				check(period);
				check(ident);
			} else if (sym == Expr()) {
				check(lbrack);
				Expr();
				check(rbrack);
			} else {
				break;
			}
		}
	}
	
	private static void Expr() {
		//Expr = ["-"] Term {Addop Term};
		if (sym == minus){
			scan();
		} else {
			break;
		}
		Term();
		while(true){
			Addop();
			Term();
		}
}
		
	private static void Factor() {
		//**Factor = Designator [ActPars]
		//| number
		//| charConst
		//| "new" ident ["[" Expr "]"]
		//| "(" Expr ")";
		if (sym == ident) {
			Designator();
			if (sym == ActPars){
				ActPars()
			} else {
				break;
			}
		} else if (sym == number) {
			check(number);
		} else if (sym == charConst) {
			check(charConst);
		} else if (sym == "new") {
			check(ident);
			if (sym == Expr) {
				check(lbrack);
				Expr();
				check(rbrack);
			}
		} else if (sym == Expr) {
			check(lpar);
			Expr();
			check(rpar);
		} else {
			error("Error at Factor");
		}
	}
		
	private static void FormPars() {
		//FormPars = Type ident {"," Type ident};
		Type();
		check(ident);
		while (true) {
			check(comma);
			Type();
			check(ident);
		}
	}
		
	private static void MethodDecl() {
		//MethodDecl = (Type | "void") ident "(" [FormPars] ")" {VarDecl} Block;
		
		while(true){
			if (sym == Type){
				Type();
			} else if () {
				check(void_);
			} else {
				error("Error at MethodDecl");
			}
		}
		check(ident);
		check(lpar);
		if (sym == FormPars) {
			check(FormPars);
		} else {
			break;
		}
		check(rpas);
		while(true){
			check(VarDecl);
		}
		check(Block);
	}

	private static void Mulop() {
		//Mulop = "*" | "/" | "%";
		
		if (sym == times) {
			check(times);
		} else if (sym == slash){
			check(slash);
		} else if (sym == rem){
			check(rem);
		} else {
			error("Invalid operator at Mulop");
		}
	}
	
	private static void Program() {

		//Program = "program" ident {ConstDecl | ClassDecl | VarDecl} '{' {MethodDecl} '}';
		
		check(program_);
		check(ident);
		while (true){
			if (sys == final_) {
				ConstDecl();
			} else if (sys == class_) {
				ClassDecl();
			} else if (sys == ident) {
				VarDecl();
			} else { 
				error("Invalid Declaration");
			}
		}
		check(lbrace);
		while(true){
			check(MethodDecl);
		}
		check(rbrace);
	}
	
	private static void Statement() {
		//Statement = Designator ("=" Expr | ActPars) ";" 
		if (sym == ident) {
			Designator();
			if (sym == assign) {
				check(assign);
				Expr();
			} else if (sym == lpar) {
				ActPars()
			} else {
				error("Assignment or call Expected");
			}
			check(semicolon);
		//| "if" "(" Condition ")" Statement ["else" Statement] 
		} else if (sym == if_) {
			scan();
			check(lpar);
			Condition();
			check(rpar);
			if (sym == "else"){
				check(else_);
				Statement();
			} else {
				break;
			}
			
			
		//| "while" "(" Condition ")" Statement
		} else if (sym == while_) {
			scan();
			check(lpar);
			Condition();
			check(rpar);
			Statement();
			
		//	| "return" [Expr] ";"
		} else if (sym == return_) {
			scan();
			if (sym == Expr){
				Expr();
			}
			check(semicolon);
		
		//| "read" "(" Designator ")" ";"
		} else if (sym == read_) {
			scan();
			check(lpar);
			Designator();
			check(rpar);
			check(semicolon);
			
		//	| "print" "(" Expr ["," number] ")" ";"
		} else if (sym == print_) {
			scan();
			check(lpar)
			Expr();
			if (sym == comma) {
				check(comma);
				number();
			}
			check(rpar);
			check(semicolon);
			
		//	| Block
		} else if (sym == Block) {
			scan();
			Block();
			
		//	| ";";
		} else if (sym == semicolon) {
			check(semicolon);
		} else {
			error("Invalid option at statement");
		}
	}
		
	private static void Relop() {
		//Relop = "==" | "!=" | ">" | ">=" | "<" | "<=";
		if (sym == eql) {
			check(eql);
		} else if (sym == neq) {
			check(neq);
		} else if (sym == gtr) {
			check(gtr)
		} else if (sym == geq) {
			check(geq)
		} else if (sym == lss) {
			check(lss)
		} else if (sym == leq) {
			check(leq)
		} else {
			Error("Invalid Operator");
		}
	}
	
	private static void Term() {
		//Term = Factor {Mulop Factor};
		Factor();
		while(true){
			Mulop();
			Factor();
		}
	}
	
	private static void Type() {
		//Type = ident ["[" "]"];
		check(ident);
		if (sym == lbrack) {
			check(lbrack);
			check(rbrack);
		} else {
			break;
		}
	}
		
	private static void VarDecl() {
		//VarDecl = Type ident {"," ident } ";";
		
		Type();
		check(ident);
		while(true){
			check(comma);
			check(ident);
		}
		check(semicolon);
	}

	public static void parse() {
		BitSet s;
		// initialize first/sync sets
		s = new BitSet(64); firstExpr = s;
		s.set(ident); s.set(number); s.set(charCon); s.set(new_); s.set(lpar); s.set(minus);

		s = new BitSet(64); firstStat = s;
		s.set(ident); s.set(if_); s.set(while_); s.set(read_);
		s.set(return_); s.set(print_); s.set(lbrace); s.set(semicolon);

		s = (BitSet)firstStat.clone(); syncStat = s;
		s.clear(ident); s.set(rbrace); s.set(eof);

		s = new BitSet(64); syncDecl = s;
		s.set(final_); s.set(ident); s.set(class_); s.set(lbrace); s.set(void_); s.set(eof);

		// start parsing
		errors = 0; errDist = 3;
		scan();
		Program();
		if (sym != eof) error("end of file found before end of program");
		if (Code.mainPc < 0) error("program contains no 'main' method");
		//Tab.dumpScope(Tab.curScope.locals);
	}
