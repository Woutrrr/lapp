package nl.wvdzwan.timemachine.callgraph.outputs;

public class NodeAnnotation {

    public static final NodeAnnotation InterfaceMethod = new NodeAnnotation("IMPLEMENTS_INTERFACE_METHOD");
    public static final NodeAnnotation InvokeInterface = new NodeAnnotation("INVOKE_INTERFACE");
    public static final NodeAnnotation InvokeVirtual = new NodeAnnotation("INVOKE_VIRTUAL");
    public static final NodeAnnotation InvokeAbstract = new NodeAnnotation("INVOKE_ABSTRACT");
    public static final NodeAnnotation PhantomNode = new NodeAnnotation("PHANTOM_NODE");
    public static final NodeAnnotation SuperMethod = new NodeAnnotation("OVERRIDES_SUPER_METHOD");

    // TODO EnumSet ?
    private String msg;

    private NodeAnnotation(String annotation) {
        this.msg = annotation;
    }

    public String getMsg() {
        return msg;
    }

    public String annotate(String msg) {
        return this.msg + "_" + msg;
    }


}
