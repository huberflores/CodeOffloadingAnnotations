package edu.ut.mobile.util;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.FileVisitResult.CONTINUE;

public class EditMojo {

    private static String PATTERN = ".java";
    static StringBuilder sb = new StringBuilder();
    /**
     * Location of the files processed after processing.
     *
     */
    private File destination;
    /**
     * The Location of the file to edit.
     *
     */
    private File source;

    public EditMojo(File src) {
        this.source = src;
    }

    /**
     * Searches for all possible
     *
     * @Cloud annotations and process them as needed
     */
    public void execute() {
        Path startingDir = Paths.get(source.getPath());
        Finder finder = new Finder(PATTERN);
        try {
            Files.walkFileTree(startingDir, finder);
        } catch (IOException e) {
            e.printStackTrace();
        }
 

    }

    /**
     * A {@code FileVisitor} that finds all files that match the specified
     * pattern.
     */
    public static class Finder extends SimpleFileVisitor<Path> {

        private final PathMatcher matcher;
        private int numMatches = 0;

        Finder(String pattern) {
            matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        }

        // Compares the glob pattern against
        // the file or directory name.
        void find(Path file) {
            Path name = file.getFileName();
            if (name != null && matcher.matches(name)) {
                numMatches++;
            }
        }

        private static void writeToFile(Path file, String data) {
            try {
                FileWriter fe = new FileWriter(file.toFile());
                fe.write(data);
                fe.flush();
                fe.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        private String changePrimitiveToWrapper(String str) {

            if ("long".equalsIgnoreCase(str)) {
                return "Long";
            } else if ("int".equalsIgnoreCase(str)) {
                return "Integer";
            } else if ("short".equalsIgnoreCase(str)) {
                return "Short";
            } else if ("char".equalsIgnoreCase(str)) {
                return "Character";
            } else if ("boolean".equalsIgnoreCase(str)) {
                return "Boolean";
            } else if ("byte".equalsIgnoreCase(str)) {
                return "Byte";
            } else if ("float".equalsIgnoreCase(str)) {
                return "Float";
            } else if ("double".equalsIgnoreCase(str)) {
                return "Double";
            } else if ("void".equalsIgnoreCase(str)) {
                return null;
            }
            return str;
        }

     

        // Invoke the pattern matching
        // method on each file.
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

            find(file);
            if (file.toString().endsWith(".java")) {
                InputStream in;
                OutputStream out;
                try {
                    in = Files.newInputStream(file);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));


                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                } catch (IOException x) {
                    System.err.println(x);
                }

                String strPlay = sb.toString();


                //clear file
                for (int i = 0; i < sb.length(); i++) {
                    sb.replace(i, i + 1, " ");
                }
                writeToFile(file, sb.toString());
                sb.delete(0, sb.length());

                sb.append(strPlay);

                //Search for the key word extends CloudService
                //            Pattern extendPattern = Pattern.compile("extends\\s*CloudActivity");
                //            Matcher extendMatcher = extendPattern.matcher(sb);
                //
                //            if(extendMatcher.find()){
                //Process the necessary classes
                Pattern pattern = Pattern.compile("@Cloud\\s*((private)|(public)|(protected))+(\\s*\\w*){2}\\s*\\((\\s*\\w*\\s{1,}\\w*\\s*(,)*)*\\)(\\s*\\w*)*\\{");
                Matcher matchers = pattern.matcher(sb);

                boolean found = false;
                int count = 0;
                while (matchers.find()) {
                    found = true;
                    //////                    System.out.format("I found the text \"%s\" starting at index %d and ending at index %d.%n", matchers.group(), matchers.start(), matchers.end());
                    //edit the class

                    Pattern params = Pattern.compile("\\((\\s*\\w*\\s{1,}\\w*\\s*(,)*)*\\)");
                    Matcher mparams = params.matcher(matchers.group());
                    mparams.find();
                    String paramTypes = "", paramValues = "";
                    String[] typesAndParams = new String[0];
                    String stp = "", stpValues = "";
                    if (!mparams.group().replace("(", "").replace(")", "").isEmpty()) {
                        typesAndParams = mparams.group().replace("(", "").replace(")", "").split(",");

                        for (int i = 0; i < typesAndParams.length; i++) {
                            typesAndParams[i] = typesAndParams[i].trim();
                            paramTypes += /*changePrimitiveToWrapper(*/ typesAndParams[i].substring(0, typesAndParams[i].indexOf(' '))/*)*/ + ".class, ";
                            paramValues += typesAndParams[i].substring(typesAndParams[i].lastIndexOf(' ') + 1) + ",";
                        }
                        stp = paramTypes.substring(0, paramTypes.lastIndexOf(",") > 0 ? paramTypes.lastIndexOf(",") : 0);
                        stpValues = paramValues.substring(0, paramValues.lastIndexOf(",") > 0 ? paramValues.lastIndexOf(",") : 0);
                    }

                    // }


                    //get ReturnType for Method
                    Pattern retType = Pattern.compile("((private)|(public)|(protected))+(\\s*\\w*){2}");
                    Matcher mret = retType.matcher(matchers.group());
                    String retString = "";
                    if (mret.find()) {
                        String[] retS = mret.group().split("\\s");
                        if (retS.length == 3) {
                            retString = changePrimitiveToWrapper(retS[retS.length - 2]);
                        }
                    }


                    //Rename method
                    Pattern methodNamePattern = Pattern.compile("\\w*\\s*\\(");
                    Matcher m = methodNamePattern.matcher(matchers.group());
                    m.find();
                    //                    System.out.format("I found M \"%s\" starting at index %d and ending at index %d.%n", m.group(), m.start(), m.end());
                    StringBuilder sm = new StringBuilder(m.group());
                    sm.insert(0, "local");


                    String originalName = matchers.group();

                    sb.replace(matchers.start() + matchers.group().indexOf(m.group()), matchers.start() + matchers.group().indexOf(m.group()) + m.group().length(), sm.toString());

                    String paramType, paramValue, paramV;

                    if (!stp.isEmpty()) {
                        paramType = "Class<?>[] paramTypes = {" + stp + "}; \n";
                    } else {
                        paramType = "Class<?>[] paramTypes = null; \n";
                    }

                    if (!stpValues.isEmpty()) {
                        paramV = "Object[] paramValues = {" + stpValues + "}; \n";
                    } else {
                        paramV = "Object[] paramValues = null; \n";
                    }

                    paramValue = "Vector results = " /*(" + retString + ") */ + "getCloudController().execute(toExecute,paramValues,this,this.getClass());";
                 

                    String resultDef = "", setresult = "", setresultexception = "", returnstr = "";
                    if (retString != null) {
                        resultDef = retString + " result = null;";
                        setresult = "\n\t\t\tresult = (" + retString + ")results.get(0);";
                        setresultexception = "result = ";
                        returnstr = "\n\treturn result;";
                    }
                    String setState = "\n\t\t\tcopyState(results.get(1));\n";
                    String callLocal = "\n\t\t\t" + setresultexception + sm.toString().substring(0, sm.length() - 1) + "(" + stpValues + ");\n";

                    String resultsConditions = "if(results != null){" + setresult + setState + "\t\t}else{" + callLocal + "\t\t}\n";

                    String MethodName = sm.toString().trim();
                    MethodName = MethodName.substring(0, MethodName.length() - 1).trim();
                    String str = "\n" + originalName.replace("@Cloud", "")
                            + "\n"
                            + "\tMethod toExecute; \n\t"
                            + paramType
                            + "\t"
                            + paramV
                            + "\t"
                            + resultDef
                            + "\n\t"
                            + "try{\n"
                            + "\t\t"
                            + "toExecute = this.getClass().getDeclaredMethod(\"" + MethodName + "\", paramTypes);\n"
                            + "\t\t"
                            + paramValue
                            + "\n\t\t"
                            + resultsConditions
                            + "\t"
                            + "}  catch (SecurityException se){\n"
                            + "\t"
                            + "} catch (NoSuchMethodException ns){\n"
                            + "\t"
                            + "}catch (Throwable th){\n"
                            + "\t"
                            + "}\n"
                            + "\t"
                            + returnstr
                            + "\n\t"
                            + "}\n"
                            + "";

                    sb.insert(sb.lastIndexOf("}") - 1, str);
                    count++;

                }

                if (found) {

                    String className = "";
                    String classSign = " " + "class ";
                    int classNameIndex = sb.indexOf(classSign) + classSign.length();
                    int indexofbracket = sb.indexOf("{", classNameIndex);
                    int indexofNextSpc = sb.indexOf(" ", classNameIndex);
                    if (indexofNextSpc < indexofbracket) {
                        className = sb.substring(classNameIndex, indexofNextSpc);
                        sb.insert(indexofNextSpc, " extends CloudRemotable ");
                    } else {
                        className = sb.substring(classNameIndex, indexofbracket);
                        sb.insert(indexofbracket, " extends CloudRemotable ");
                    }

                    int indexofimport = sb.lastIndexOf("import");
                    int indexofscolon = sb.indexOf(";");
                    if (indexofscolon < classNameIndex) {
                        sb.insert(indexofscolon + 1, "\n\nimport java.lang.reflect.Method; \n" + "import edu.ut.mobile.network.CloudRemotable;\n"
                                + "import java.util.Vector;\n");
                    } else {
                        sb.insert(0, "\nimport java.lang.reflect.Method; \n" + "import edu.ut.mobile.network.CloudRemotable;\n"
                                + "import java.util.Vector;\n");
                    }

                    StringBuffer copyStateBody = new StringBuffer("\n\t");
                    copyStateBody.append(className + " localstate = (" + className + ") state;");

                    //  finding class instance variables
                    Vector<String> InstanceVars = findInstanceVars(sb);
                    //
                    for (int r = 0; r < InstanceVars.size(); r++) {
                        //                        System.out.println(InstanceVars.get(r));
                        copyStateBody.append("\n\t" + "this." + InstanceVars.get(r) + " = localstate." + InstanceVars.get(r) + ";");
                    }

                    String copyStateMethod = "\n" + "void copyState(Object state){" + copyStateBody + "\n}";
                    sb.insert(sb.lastIndexOf("}") - 1, copyStateMethod);
                }

                writeToFile(file, sb.toString());
                sb.delete(0, sb.length());
            }
            return CONTINUE;
        }

        // Invoke the pattern matching
        // method on each directory.
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            System.err.println(exc);
            return CONTINUE;
        }
    }

    static Vector<String> findInstanceVars(StringBuilder classContent) { //classContent contains java class file content
        Vector<String> InstanceVars = new Vector<String>();

        StringBuffer initialsb = new StringBuffer(classContent.toString());

        deleteHardCodedStrings(initialsb); // delete hard coded strings of class
        deleteComments(initialsb);  // delete all comments

        String sb = initialsb.toString();
        int indexofFirstBracket = sb.indexOf("{");
        int indexofLastBracket = sb.lastIndexOf("}");

        sb = sb.substring(indexofFirstBracket + 1, indexofLastBracket + 1);

        String[] splits = sb.split(";");

        int TotalnumofOpenBrackets = 0;

        for (int i = 0; i < splits.length; i++) {
            int numofOpenBrackets = numofchar(splits[i], '{');
            int numofClosedBrackets = numofchar(splits[i], '}');

            TotalnumofOpenBrackets += numofOpenBrackets;
            TotalnumofOpenBrackets -= numofClosedBrackets;

            if (TotalnumofOpenBrackets == 0) {
                String varStr = "";
                if (numofOpenBrackets != 0 && numofClosedBrackets != 0) {
                    varStr = deleteBrackets(splits[i]);
                } else if (numofClosedBrackets != 0) {
                    varStr = splits[i].substring(splits[i].lastIndexOf('}') + 1);
                } else {
                    varStr = splits[i];
                }
//                      System.out.println(varStr);
                if (varStr.indexOf(" final ") == -1) {
                    String[] varSplits = varStr.split(",");
                    for (int j = 0; j < varSplits.length; j++) {
                        int indexofEqualSign = varSplits[j].indexOf('=');
                        if (indexofEqualSign != -1) {
                            varSplits[j] = varSplits[j].substring(0, indexofEqualSign).replace("[]", " ").trim();
                            String varName = varSplits[j].substring(varSplits[j].lastIndexOf(" ") + 1);
                            InstanceVars.add(varName);
                        } else {
                            varSplits[j] = varSplits[j].replace("[]", " ").trim();
                            String varName = varSplits[j].substring(varSplits[j].lastIndexOf(" ") + 1);
                            InstanceVars.add(varName);
                        }

                    }
                }

            }

        }

        return InstanceVars;
    }

    static String deleteBrackets(String str) {
        while (true) {
            int openbracketIndex = str.indexOf("{");
            if (openbracketIndex != -1) {
                int closedbracketIndex = str.indexOf("}", openbracketIndex + 1);
                if (closedbracketIndex != -1) {
                    str = str.substring(0, openbracketIndex) + str.substring(closedbracketIndex + 1);
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        return str;
    }

    static void deleteHardCodedStrings(StringBuffer sb) {
        String[] lineSplits = sb.toString().split("\n");
        sb.delete(0, sb.length());

        for (int i = 0; i < lineSplits.length; i++) {
            while (true) {
                int quoteIndexSt = lineSplits[i].indexOf("\"");
                if (quoteIndexSt != -1) {
                    int quoteIndexEnd = lineSplits[i].indexOf("\"", quoteIndexSt + 1);
                    if (quoteIndexEnd != -1) {
                        lineSplits[i] = lineSplits[i].substring(0, quoteIndexSt) + lineSplits[i].substring(quoteIndexEnd + 1);
                    } else {
                        break;
                    }
                } else {
                    break;
                }
            }
            lineSplits[i] = lineSplits[i] + "\n";
            sb.append(lineSplits[i]);
        }
    }

    static void deleteComments(StringBuffer sb) {

        // removing multi-line comments
        while (true) {
            int bigComIndexSt = sb.indexOf("/*");
            if (bigComIndexSt != -1) {
                int bigComIndexEnd = sb.indexOf("*/");
                if (bigComIndexEnd != -1) {
                    sb.delete(bigComIndexSt, bigComIndexEnd + 2);
                } else {
                    break;
                }
            } else {
                break;
            }
        }

        // removing single line comments
        String[] lineSplits = sb.toString().split("\n");
        sb.delete(0, sb.length());

        for (int i = 0; i < lineSplits.length; i++) {
            int indexofsmallcomment = lineSplits[i].indexOf("//");
            if (indexofsmallcomment != -1) {
                lineSplits[i] = lineSplits[i].substring(0, lineSplits[i].indexOf("//"));
            }

            lineSplits[i] = lineSplits[i] + "\n";
            sb.append(lineSplits[i]);
        }

    }

    static int numofchar(String st, char c) {
        int num = 0;
        for (int i = 0; i < st.length(); i++) {
            if (st.charAt(i) == c) {
                num++;
            }
        }

        return num;
    }
}
