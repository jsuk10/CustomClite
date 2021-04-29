import java.io.Console;
import java.util.*;
import java.util.Scanner;

//지정문을 쓸때
// f = (i2f) i로 형변환
// int +, float +를 구분하는 역활
public class TypeTransformer {
    // 프로그램을 받아서 변환된 프로그램을 반환함.
    // body만 변환함
    public static Program T (Program p, TypeMap tm) {
        Block body = (Block)T(p.body, tm);
        return new Program(p.decpart, body);
    } 

    //식 변환
    public static Expression T (Expression e, TypeMap tm) {
        //값과 변수일 경우 그냥 반환해줌
        if (e instanceof Value) 
            return e;
        if (e instanceof Variable) 
            return e;
        //이항식일 경우
        if (e instanceof Binary) {
            Binary b = (Binary)e; 
            Type typ1 = StaticTypeCheck.typeOf(b.term1, tm);
            Type typ2 = StaticTypeCheck.typeOf(b.term2, tm);
            //식을 두개 다 변환함
            Expression t1 = T (b.term1, tm);
            Expression t2 = T (b.term2, tm);
            //식의 첫번째 타입을 검사하고
            //이에 맞는 변환을 함
            //예를들어 t1 - t2일 경우 t들의 타입에 맞는 연산자를 만들어 할당해줌줌
            //AbstractSyntax.java참조  pule를 넣으면 int_plus로 바꿔줌.
            if (typ1 == Type.INT)
                return new Binary(b.op.intMap(b.op.val), t1,t2);
            else if (typ1 == Type.FLOAT) 
                return new Binary(b.op.floatMap(b.op.val), t1,t2);
            else if (typ1 == Type.CHAR) 
                return new Binary(b.op.charMap(b.op.val), t1,t2);
            else if (typ1 == Type.BOOL) 
                return new Binary(b.op.boolMap(b.op.val), t1,t2);
            throw new IllegalArgumentException("should never reach here");
        }
        // Unary일 경우
        // not일 경우 boolmap을 참조하여
        // 형변환 연산도 넣어줘야함.
        // student exercise
        if (e instanceof Unary) {
            Unary u = (Unary)e;
            Type typ = StaticTypeCheck.typeOf(u.term, tm);
            Expression ex = T (u.term, tm);
            //bool이고 부정 op일 경우 부정 연산자 삽입
            if(typ == Type.BOOL && u.op.NotOp() ){
                return new Unary(u.op.boolMap(u.op.val),ex);
            }else if(u.op.NegateOp()){
                if (typ == Type.INT){
                    return new Unary(u.op.intMap(u.op.val),ex);
                }else if (typ == Type.FLOAT) {
                    return new Unary(u.op.floatMap(u.op.val),ex);
                }
            }
            //형변환은 검사해주는 메서드가 없어서 추가함.
            //op가 int에서 변환 할 경우
            else if(typ == Type.INT && (u.op.IntCast()))
                    return new Unary(u.op.intMap(u.op.val), ex);
            //op가 float에서 변환 할 경우
            else if ((typ == Type.FLOAT) && (u.op.FloatCast()))
                return new Unary(u.op.floatMap(u.op.val), ex);
            //op가 char에서 변환 할 경우
            else if ((typ == Type.CHAR) && (u.op.CharCast()))
                return new Unary(u.op.charMap(u.op.val), ex);

        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static Statement T (Statement s, TypeMap tm) {
        if (s instanceof Skip) return s;
        //지정문만 복잡하다.
        //지정문인 경우 int = float인 경우가 있고, char = int인 경우가 있다.
        if (s instanceof Assignment) {
            Assignment a = (Assignment)s;
            Variable target = a.target;
            Expression src = T (a.source, tm);
            Type ttype = (Type)tm.get(a.target);
            Type srctype = StaticTypeCheck.typeOf(a.source, tm);
            if (ttype == Type.FLOAT) {
                if (srctype == Type.INT) {
                    //int를 float로 바꾸는 변환
                    src = new Unary(new Operator(Operator.I2F), src);
                    srctype = Type.FLOAT;
                }
            }
            else if (ttype == Type.INT) {
                if (srctype == Type.CHAR) {
                    //char을 int로 변경
                    src = new Unary(new Operator(Operator.C2I), src);
                    srctype = Type.INT;
                }
            }
            StaticTypeCheck.check( ttype == srctype,
                      "bug in assignment to " + target);
            return new Assignment(target, src);
        }
        // if 수식일 경우우
        if (s instanceof Conditional) {
            Conditional c = (Conditional)s;
            //테스트 변경
            Expression test = T (c.test, tm);
            //then,else도 바꿈
            Statement tbr = T (c.thenbranch, tm);
            Statement ebr = T (c.elsebranch, tm);
            return new Conditional(test,  tbr, ebr);
        }
        // Loop수식일 경우
        if (s instanceof Loop) {
            Loop l = (Loop)s;
            Expression test = T (l.test, tm);
            Statement body = T (l.body, tm);
            return new Loop(test, body);
        }
        //Block일 경우
        if (s instanceof Block) {
            Block b = (Block)s;
            Block out = new Block();
            for (Statement stmt : b.members)
                out.members.add(T(stmt, tm));
            return out;
        }
        throw new IllegalArgumentException("should never reach here");
    }

    public static void Put(Expression e, TypeMap tm){
        System.out.println("\nexpression is\n");
        StaticTypeCheck.V(e,tm);
        //e.display();
        System.out.println("Put result");
        //case 1 값일 경우
        if (e instanceof Value){
            Value v = (Value) e;
            v.PrintValue();
            return;
        }
        //case 2 변수일 경우 타입이 맵에 있는지 확인
        if (e instanceof Variable) {
            Variable v = (Variable) e;
            // 타입이 타당한지 확인
            StaticTypeCheck.check(tm.containsKey(v), "undefined variable: " + v);
            System.out.println(v.toString());
            return;
        }
        //case 3 이항식(Binary)일 경우
        if (e instanceof Binary) {
            Binary b = (Binary) e;
            StaticTypeCheck.V(b,tm);
            System.out.println(b.term1.toString() +" "+ b.op.val +" " + b.term2.toString() );
            }
        // Unary인 경우
        if(e instanceof Unary){
            Unary u = (Unary) e;
            System.out.println(u.op.val +" " + u.term.toString() );
            return;
        }
    }

    public static int GetInt(){
        Scanner scanner = new Scanner(System.in);
        int temp = scanner.nextInt();
        return temp;
    }

    public static float GetFloat(){
        Scanner scanner = new Scanner(System.in);
        float temp = scanner.nextFloat();
        return temp;
    }

    public static void main(String args[]) {

        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();           // student exercise
        System.out.println("\nBegin type checking...");
        System.out.println("Type map:");
        TypeMap map = StaticTypeCheck.typing(prog.decpart);
        map.display();    // student exercise
        StaticTypeCheck.V(prog);
        Program out = T(prog, map);
        System.out.println("Output AST");
        out.display();    // student exercise
    } //main

} // class TypeTransformer

    
