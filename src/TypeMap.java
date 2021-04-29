import java.util.*;

public class TypeMap extends HashMap<Variable, Type> {
    public void display() {
            //키와 벨류의 모든값을 출력하는 HashMap.entrySet()사용
            System.out.println(this.entrySet());
        }
// TypeMap is implemented as a Java HashMap.  
// Plus a 'display' method to facilitate experimentation.
}

