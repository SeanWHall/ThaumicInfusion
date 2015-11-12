/*
 * @author TheDrunkMafia
 *
 * See http://www.wtfpl.net/txt/copying for licence
 */

package drunkmafia.thaumicinfusion.common.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.Opcodes.*;

public class Interface {

    private final String className;
    private final List<IMethod> methods = new ArrayList<IMethod>();

    public Interface(String className) {
        this.className = className;
    }

    public void addMethod(IMethod method) {
        this.methods.add(method);
    }

    public void injectMethodsIntoClass(ClassNode node) {
        node.interfaces.add(this.className);
        for (IMethod method : this.methods)
            node.methods.add(method.getMethodNode(node.name));
    }

    public List<IMethod> getMethods() {
        return this.methods;
    }

    public String getClassName() {
        return this.className;
    }
}

class IMethod {

    private final String name;
    private final String returnType;
    private final String paramaters;

    public IMethod(String name, String returnType, String paramaters) {
        this.name = name;
        this.returnType = returnType;
        this.paramaters = paramaters;
    }

    public MethodNode getMethodNode(String className) {
        MethodNode node = new MethodNode(1, this.name, "(" + this.paramaters + ")" + this.returnType, null, null);
        node.localVariables = new ArrayList<LocalVariableNode>();
        node.localVariables.add(new LocalVariableNode("this", "L" + className + ";", null, new LabelNode(), new LabelNode(), 0));

        if (this.returnType != null) {
            InsnList list = new InsnList();
            int opcode = Type.getReturnType(node.desc).getOpcode(IRETURN);
            list.add(new LdcInsnNode(opcode == IRETURN || opcode == FRETURN || opcode == DRETURN || opcode == LRETURN ? 0 : null));
            list.add(new InsnNode(opcode));
            node.instructions.add(list);
        }
        return node;
    }

    public String getName() {
        return this.name;
    }

    public String getReturnType() {
        return this.returnType;
    }

    public String getParamaters() {
        return this.paramaters;
    }
}