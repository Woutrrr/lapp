package nl.wvdzwan.lapp;

public class ClassPathFileTest {

//
//    @Test
//    void mainJarTest() {
////
////        String content = "main.jar\n"
////                + "dep1.jar\n"
////                + "dep2.jar\n"
////                + "dep3.jar\n";
////        List<String> deps = Arrays.asList("j/dep1.jar", "j/dep2.jar", "j/dep3.jar");
////        BufferedReader reader = new BufferedReader(new StringReader(content));
////
////        ClassPathFile cp = new ClassPathFile(reader, Paths.get("j"));
////
////        assertEquals("j/main.jar", cp.getMainJar());
////        assertEquals(deps, cp.getDependencies());
//    }
//
//
//    @Test
//    void absoluteDependencyPaths() {
////        String content = "main.jar\n"
////                + "/var/dep1.jar\n"
////                + "/var/dep2.jar\n"
////                + "/var/dep3.jar\n";
////        List<String> deps = Arrays.asList("/var/dep1.jar", "/var/dep2.jar", "/var/dep3.jar");
////        BufferedReader reader = new BufferedReader(new StringReader(content));
////
////        ClassPathFile cp = new ClassPathFile(reader, Paths.get("jarFolder"));
////
////        assertEquals("jarFolder/main.jar", cp.getMainJar());
////        assertEquals(deps, cp.getDependencies());
//    }
//
//    @Test
//    void absoluteMainJarPath() {
////        String content = "/var/main.jar\n";
////        BufferedReader reader = new BufferedReader(new StringReader(content));
////
////        ClassPathFile cp = new ClassPathFile(reader, Paths.get("jarFolder"));
////
////        assertEquals("/var/main.jar", cp.getMainJar());
//
//    }
//
//    @Test
//    void mixedRelativeAbsoluteDependencyPaths() {
////        String content = "main.jar\n"
////                + "/var/dep1.jar\n"
////                + "dep2.jar\n"
////                + "/var/dep3.jar\n";
////        List<String> deps = Arrays.asList("/var/dep1.jar", "jarFolder/dep2.jar", "/var/dep3.jar");
////        BufferedReader reader = new BufferedReader(new StringReader(content));
////
////        ClassPathFile cp = new ClassPathFile(reader, Paths.get("jarFolder"));
////
////        assertEquals("jarFolder/main.jar", cp.getMainJar());
////        assertEquals(deps, cp.getDependencies());
//    }
//
//
//    @Test
//    void noDependencies() {
//        String content = "main.jar\n";
//        BufferedReader reader = new BufferedReader(new StringReader(content));
//
//        ClassPathFile cp = new ClassPathFile(reader, Paths.get("jars"));
//
//        assertEquals("jars/main.jar", cp.getMainJar());
//        assertEquals(Collections.EMPTY_LIST, cp.getDependencies());
//    }
//
//
//    @Test
//    void emptyFile() {
//        String content = "";
//        BufferedReader reader = new BufferedReader(new StringReader(content));
//
//        assertThrows(IllegalArgumentException.class, () -> {
//            new ClassPathFile(reader, Paths.get("jars"));
//        });
//
//
//    }
}
