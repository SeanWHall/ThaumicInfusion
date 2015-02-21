package drunkmafia.thaumicinfusion.common.util;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by DrunkMafia on 25/07/2014.
 * <p/>
 * See http://www.wtfpl.net/txt/copying for licence
 */
public class SafeClassGenerator {
    private Logger log;
    private ClassPool cp = ClassPool.getDefault();

    private static final Map<String, String> primitiveReturn = new HashMap<String, String>();
    private Map<CtClass, Class> classToSafe = new HashMap<CtClass, Class>();
    private CtClass lowestSuper;

    static{
        primitiveReturn.put("boolean", "false");
        primitiveReturn.put("byte", "0");
        primitiveReturn.put("short", "0");
        primitiveReturn.put("int", "0");
        primitiveReturn.put("float", "0F");
        primitiveReturn.put("double", "0.0D");
        primitiveReturn.put("string", "");
    }

    public SafeClassGenerator(ClassPool cp){
        this.cp = cp;
    }

    /**
     * @param ct The class which will be the lowest the method lookup should go to
     */
    public void lowestSuper(CtClass ct){
        lowestSuper = ct;
    }

    /**
     * Will enable debugging while generating a safe version of a class
     */
    public void setLog(Logger log){
        this.log = log;
    }

    public <T>T getSafeObjClass(Class<T> original){
        return getSafeObjClass(original, new String[0]);
    }

    /**
     * @param original The class which is going to be recreated
     * @param blacklist Any methods that should not call the exception handling
     * @return A object version of the safe class
     */
    public <T>T getSafeObjClass(Class<T> original, String[] blacklist){
        Class safeClass = generateSafeClass(getCtClass(original), blacklist);
        if(safeClass != null){
            try{
                return (T) safeClass.newInstance();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return null;
    }

    public Class generateSafeClass(CtClass orginal){
        return generateSafeClass(orginal, new String[0]);
    }

    /**
     * This will generate a safe version of the passed in class, this means every method down
     * to the lowest set super will be overridden and invoked within a try & catch for exception
     * handling
     *
     * @param orginal The class which is going to be recreated
     * @param blacklistedMethods Any methods that should not call the exception handling
     * @return Will return a safe version of the passed in class
     */
    public Class generateSafeClass(CtClass orginal, String[] blacklistedMethods) {
        if(orginal == null)
            return null;

        if(orginal.getClassFile().isFinal() || orginal.getClassFile().isInterface())
            throw new ClassFormatError("Class: " + orginal.getName() + " has an incompatible access or is an interface");

        if(classToSafe.containsKey(orginal))
            return classToSafe.get(orginal);

        try{
            CtMethod[] allMethods = getMethodsFromSuper(orginal);

            CtClass safe = cp.makeClass("safe." + orginal.getName(), orginal);
            CtClass exception = getCtClass(Exception.class);

            if(safe.isFrozen())
                safe.defrost();
            safe.stopPruning(true);

            for(CtMethod method : allMethods){
                try{
                    CtMethod safeMethod = new CtMethod(method.getReturnType(), method.getName(), method.getParameterTypes(), safe);
                    safeMethod.getMethodInfo().setAccessFlags(method.getMethodInfo().getAccessFlags());

                    boolean hasReturn = !safeMethod.getReturnType().getName().equals("void"), blackListed = false;
                    String retType = safeMethod.getReturnType().getName(), ret = primitiveReturn.containsKey(retType) ? primitiveReturn.get(retType) : "(" + retType + ")null";

                    for(String black : blacklistedMethods) {
                        if (method.getName().equals(black)) {
                            blackListed = true;
                            break;
                        }
                    }

                    safeMethod.setBody("{ " + (hasReturn ? " return " : "") + "super." + safeMethod.getName() + "($$); }");
                    safeMethod.addCatch("{ " + (blackListed ? "" :"drunkmafia.thaumicinfusion.common.block.InfusedBlock.handleError($e, this); ") + (hasReturn ? " return " + ret + "; }" : "return; }"), exception);
                    safe.addMethod(safeMethod);
                }catch(Exception e){
                    if(log != null)
                        log.error("Method: " + method.getName() + " \n Class: " + method.getDeclaringClass().getName() + " \n Access Level: " + method.getMethodInfo().getAccessFlags(), e);
                }
            }

            Class clazz = safe.toClass();
            safe.writeFile("ThaumicInfusion Safe Classes");
            classToSafe.put(orginal, clazz);
            return clazz;
        }catch(Exception e){
            if(log != null)
                log.error("Failed while registering class", e);
        }
        return null;
    }

    /**
     * @return returns a Javassist version of the passed in class
     */
    public CtClass getCtClass(Class c){
        try{
            cp.appendClassPath(new javassist.LoaderClassPath(c.getClassLoader()));
            return cp.get(c.getName());
        }catch (Exception e){
            if(log != null)
                log.error("Error getting: " + c.getName(), e);
        }
        return null;
    }

    /**
     * Will find every method that has been overriden down to the set lowest super
     *
     * @param startSuper The class which the lookup will start from
     * @return Array of methods found
     */
    public CtMethod[] getMethodsFromSuper(CtClass startSuper){
        ArrayList<CtMethod> methodsBlack = new ArrayList<CtMethod>();
        ArrayList<CtMethod> methods = new ArrayList<CtMethod>();
        try{
            CtClass currentSuper = startSuper;

            while(currentSuper != lowestSuper){
                for(CtMethod method : currentSuper.getDeclaredMethods()){
                    if(getMethodInArray(methodsBlack, method) != null)
                        continue;

                    if(getMethodInArray(methods, method) != null || !isMethodCompatible(method) || method.isEmpty()) {
                        methodsBlack.add(method);
                        continue;
                    }
                    methods.add(method);
                }
                currentSuper = currentSuper.getSuperclass();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return methods.toArray(new CtMethod[methods.size()]);
    }

    /**
     * @param list Array to look though
     * @param method Method which is being search for
     * @return The version of the method stored in the array
     */
    public CtMethod getMethodInArray(ArrayList<CtMethod> list, CtMethod method){
        for(CtMethod methList : list) {
            try {
                if (methList.getName().matches(method.getName()) && methList.getReturnType() == method.getReturnType() && methList.getParameterTypes().length == method.getParameterTypes().length)
                    return methList;
            }catch (Exception e){}
        }
        return null;
    }

    public boolean isMethodCompatible(CtMethod meth){
        String methCheck = meth.toString();
        return (methCheck.contains("public") || methCheck.contains("protected")) && !methCheck.contains("final") && !methCheck.contains("static") && !methCheck.contains("abstract");
    }
}
