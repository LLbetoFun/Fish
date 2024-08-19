import org.objectweb.asm.ClassWriter;

public class MyClassWriter extends ClassWriter {
    public MyClassWriter(int flags) {
        super(flags);
    }

    @Override
    protected String getCommonSuperClass(String type1, String type2) {
        try {
            return super.getCommonSuperClass(type1, type2);
        }
        catch (Exception e) {
            e.printStackTrace();
            return type1;
        }
    }
}
