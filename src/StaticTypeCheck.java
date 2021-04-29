// StaticTypeCheck.java

import java.util.*;

// Static type checking for Clite is defined by the functions 
// V and the auxiliary functions typing and typeOf.  These
// functions use the classes in the Abstract Syntax of Clite.


public class StaticTypeCheck {

    //타입맵을 만드는 함수
    //이때 TypeMap는 해쉬 테이블(Map)이다.
    public static TypeMap typing (Declarations d) {
        TypeMap map = new TypeMap();
        //빈 타입맵 생성
        for (Declaration di : d) 
            map.put (di.v, di.t);
        //맵에 타입과 값을 맞춰서 넣기
        return map;
    }

    //이게 맞는지 테스트 해보는 함수(true이면 아무것도 안하고 아니면 오류 출력)
    public static void check(boolean test, String msg) {
        if (test)  return;
        System.err.println(msg);
        System.exit(1);
    }

    //이상이 없으면 넘어가고 있으면 프로그램 종료
    public static void V (Declarations d) {
        for (int i=0; i<d.size() - 1; i++)
            for (int j=i+1; j<d.size(); j++) {
                Declaration di = d.get(i);
                Declaration dj = d.get(j);
                check( ! (di.v.equals(dj.v)),"duplicate declaration: " + dj.v);
                //i와 j가 같은지 즉 키값이 동일한지 체크함
            }
    } 

    //프로그램이 타당한지
    //즉 선언파트와 몸체(body)파트가 타입맵에 대해 타당한지
    public static void V (Program p) {
        V (p.decpart);
        V (p.body, typing (p.decpart));
    } 

    //수식을 주면 타입을 반환하는 함수이다.
    public static Type typeOf (Expression e, TypeMap tm) {
        //case 1 수식이 값일 경우
        if (e instanceof Value) return ((Value)e).type;
        //case 2 수식이 변수일 경우
        if (e instanceof Variable) {
            Variable v = (Variable)e;
            check (tm.containsKey(v), "undefined variable: " + v);
            //타입맵이 변수를 포함하는가 확인
            return (Type) tm.get(v);
            //get을 이용해서 키값(value라고 사용하지만 name이라고 봐도 무방)을 통해 value(타입값)을 반환
        }

        //case 3 이항식(Binary)일 경우
        if (e instanceof Binary) {
            Binary b = (Binary)e;
            //case 1 산술 연산자일 경우 float || int 타입 반환
            if (b.op.ArithmeticOp( ))
                if (typeOf(b.term1,tm)== Type.FLOAT)
                    return (Type.FLOAT);
                else return (Type.INT);
            //case 2 관계 연산자일 경우 || Bool 타입일 경우 Bool값을 리턴한다.
            if (b.op.RelationalOp( ) || b.op.BooleanOp( ))
                return (Type.BOOL);
        }

        //case 4 단항연산자(Unary)일 경우
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            //각각 !, -, 케스팅 연산자 즉 형변환 연산 (int)같은 것들이다.
            if (u.op.NotOp( ))        return (Type.BOOL);
            else if (u.op.NegateOp( )) return typeOf(u.term,tm);
            else if (u.op.intOp( ))    return (Type.INT);
            else if (u.op.floatOp( )) return (Type.FLOAT);
            else if (u.op.charOp( ))  return (Type.CHAR);
        }
        throw new IllegalArgumentException("should never reach here");
    } 

    //식의 타당성을 검사하는 함수로 식과 맵을 받음
    public static void V (Expression e, TypeMap tm) {
        //case 1 값일 경우
        if (e instanceof Value)
            return;
        //case 2 변수일 경우 타입이 맵에 있는지 확인
        if (e instanceof Variable) { 
            Variable v = (Variable)e;
            check( tm.containsKey(v), "undeclared variable: " + v);
            return;
        }
        //case 3 이항식(Binary)일 경우
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            //각각의 양항을 나누고 이게 타당한지 검사함
            Type typ1 = typeOf(b.term1, tm);
            Type typ2 = typeOf(b.term2, tm);
            V (b.term1, tm);
            V (b.term2, tm);
            //산술 연산자일 경우 타입이 같아야하고, 둘다 int or float여야 한다.
            //Clite는 int float가 섞인 mixed는 허용 안함
            if (b.op.ArithmeticOp( ))  
                check( typ1 == typ2 &&(typ1 == Type.INT || typ1 == Type.FLOAT),
                        "type error for " + b.op);
            //관계 연산자면 타입이 같아야함
            else if (b.op.RelationalOp( ))
                check( typ1 == typ2 , "type error for " + b.op);
            //bool연산자인 경우도 마찬가지
            else if (b.op.BooleanOp( )) 
                check( typ1 == Type.BOOL && typ2 == Type.BOOL,b.op + ": non-bool operand");
            else
                throw new IllegalArgumentException("should never reach here");
            return;
        }
        // student exercise
        // Unary인 경우를 만들어야한다.
        if(e instanceof  Unary){
            Unary u = (Unary) e;
            Type typ = typeOf(u.term, tm);
            //식이 타당한지 검사
            V (u.term, tm);
            if(u.op.NotOp()){
                check( typ == Type.BOOL, "type error for " + u.op);
            }
            else if(u.op.NegateOp()){
                check(typ == Type.INT || typ == Type.FLOAT,"type error for " + u.op);
            }else{
                throw new IllegalArgumentException("should never reach here" + u.op + "err");
            }

        }
        throw new IllegalArgumentException("should never reach here");
    }

    //문장의 타당성을 체크한다.
    public static void V (Statement s, TypeMap tm) {
        //s가 없을 경우 예외 발생
        if ( s == null )
            throw new IllegalArgumentException( "AST error: null statement");
        //case 1 skip일 경우는 아무것도 안함
        if (s instanceof Skip) return;
        //case 2 지정문(할당)일 경우
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            //타입맵에 선언이 되어 있는지
            check( tm.containsKey(a.target), " undefined target in assignment: " + a.target);
            //식이 타당한가.
            V(a.source, tm);
            //타겟의 타입을 얻어옴.
            Type ttype = (Type)tm.get(a.target);
            //소스의 타입을 얻어옴.
            Type srctype = typeOf(a.source, tm);

            //같으면 문제 없음.
            // 두개가 다를 경우 얘외 케이스만 찾아줌
            if (ttype != srctype) {
                //float일때만 허용해 주는데 이후 타입이int이거나 char일때만 허용
                if (ttype == Type.FLOAT)
                    check( srctype == Type.INT
                           , "mixed mode assignment to " + a.target);
                else if (ttype == Type.INT)
                    check( srctype == Type.CHAR
                           , "mixed mode assignment to " + a.target);
                //에러 출력
                else
                    check( false
                           , "mixed mode assignment to " + a.target);
            }
            return;
        }
        // if, while, block문 추가하기 (3가지)
        //while문 같은 경우에는 test 타당과 불린이라는 조건과 과 body가 타당한지 찾아야함.
        //if는 테스트 테스트식, then, else 가 타당해야하고 테스트식이 bool타입이여야한다.
        // student exercise
        if(s instanceof  Conditional){
            Conditional c = (Conditional)s;
            //테스트 식이 타당한가
            V(c.test, tm);
            V(c.thenbranch,tm);
            V(c.elsebranch,tm);
            check( typeOf(c.test,tm) == Type.BOOL,"test is not bool" + c.test);
            return;
        }
        if(s instanceof Loop){
            Loop l = (Loop)s;
            V(l.test,tm);
            V(l.body,tm);
            check( typeOf(l.test,tm) == Type.BOOL,"test is not bool" + l.test);
            return;
        }
        if(s instanceof Block){
            Block b = (Block) s;
            for(Statement i : b.members) {
                V(i, tm);
            }
            return;
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();           // student exercise
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
        TypeMap map = typing(prog.decpart);
        map.display();   // student exercise
        V(prog);
    } //main

} // class StaticTypeCheck

