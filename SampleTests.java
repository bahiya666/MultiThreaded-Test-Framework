public class SampleTests {
    private void simulateWork(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Annotation.Test
    @Annotation.Order(1)
    public void testA() {
        System.out.println("Running Test A");
        simulateWork(100); //give it 100ms of work
    }

    @Annotation.Test
    @Annotation.Order(2)
    public void testB() {
        System.out.println("Running Test B");
        simulateWork(200);
    }

    @Annotation.Test
    @Annotation.Order(3)
    @Annotation.DependsOn("testB")
    public void testC() {
        System.out.println("Running Test C");
        simulateWork(150);
    }

    @Annotation.Test
    @Annotation.Order(4)
    public void testD() {
        simulateWork(180);
        throw new RuntimeException("Test D failed");

    }

    @Annotation.Test
    @Annotation.Order(5)
    public void testE() {
        System.out.println("Running Test E");
        simulateWork(120);
    }

    @Annotation.Test
    @Annotation.Order(6)
    public void testF() {
        System.out.println("Running Test F");
        simulateWork(90);
    }

    @Annotation.Test
    @Annotation.Order(7)
    public void testG() {
        System.out.println("Running Test G");
        simulateWork(250);
    }

    @Annotation.Test
    @Annotation.Order(8)
    public void testH() {
        System.out.println("Running Test H");
        simulateWork(170);
    }

    @Annotation.Test
    @Annotation.Order(9)
    public void testI() {
        System.out.println("Running Test I");
        simulateWork(130);
    }

    @Annotation.Test
    @Annotation.Order(10)
    public void testJ() {
        simulateWork(110);
        throw new RuntimeException("Test J failed"); //TEST BEHAVIOUR VERIFICATION 

    }

    @Annotation.Test

    @Annotation.DependsOn("testG")
    public void testK() {
        System.out.println("Running Test K");
        simulateWork(140);
    }

    @Annotation.Test
    public void testL() {
        System.out.println("Running Test L");
        simulateWork(80);
    }
}
