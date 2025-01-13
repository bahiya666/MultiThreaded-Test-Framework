import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Annotation{

    //@Test annotation
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Test {}

    //@Order: for prioritizing tests
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Order {
        int value() default Integer.MAX_VALUE; //if no order no. is given, the default will be that it runs last
    }

    // task 3: annotation for specifying dependencies between tests
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface DependsOn {
        String[] value();  //names of methods the test depends on
    }





    //discovering and executing the test methods using reflection api
    public static void runTests(Class<?> testClass) {
        Method[] methods = testClass.getDeclaredMethods();
        List<Method> testMethods = new ArrayList<>();


        //discover methods annotated with @Test
        for (Method method : methods) {
            if (method.isAnnotationPresent(Test.class)) {
                testMethods.add(method);
            }
        }

        //sort based on @Order annotation
        testMethods.sort(Comparator.comparingInt(method -> {
            Order order = method.getAnnotation(Order.class);
            return order != null ? order.value() : Integer.MAX_VALUE;
        }));


        Object testInstance;        //create an instance oftest class and execute test method
        try {
            testInstance = testClass.getDeclaredConstructor().newInstance();
            for (Method testMethod : testMethods) {
                System.out.println("Executing: " + testMethod.getName());
                testMethod.invoke(testInstance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Sample test


}