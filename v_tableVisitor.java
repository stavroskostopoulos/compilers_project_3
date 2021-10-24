import syntaxtree.*;
import java.util.*;
import visitor.*;

import java.io.FileWriter;   // Import the FileWriter class



class v_tableVisitor extends GJDepthFirst<String, String> {

    String current_class=null;
    Integer methodCounter = 0; //a variable to keep the number of methods each class has

    //this holds for every classname , its methods references in clang for the vtable (in String format)
    //for every class ,there is a HashMap that uses as key a method name (ex. set)
    //and as value to that key the clang vtable ref ( ex. i8* bitcast (i32 (i8*,i32)* @Derived.set to i8*) )
    //that way when we have an extended class declaration we can also get the extended class methods' clang references
    LinkedHashMap<String, LinkedHashMap<String, String>> class_methodDetails = new LinkedHashMap<String, LinkedHashMap<String, String>>();

    vtableTypes vtable_methodtype_table = null; 
    vtableTypes vtable_vartype_table = null; 
    vtableTypes vtable_puremethtype_table = null;
    

    FileWriter myWriter = null;
    
    


    v_tableVisitor(FileWriter writer, vtableTypes  methtypeTable, vtableTypes  vartypeTable, vtableTypes  puremethtypeTable){
        myWriter=writer;
        vtable_methodtype_table = methtypeTable;
        vtable_vartype_table = vartypeTable;
        vtable_puremethtype_table = puremethtypeTable;

    }

    

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> "public"
     * f4 -> "static"
     * f5 -> "void"
     * f6 -> "main"
     * f7 -> "("
     * f8 -> "String"
     * f9 -> "["
     * f10 -> "]"
     * f11 -> Identifier()
     * f12 -> ")"
     * f13 -> "{"
     * f14 -> ( VarDeclaration() )*
     * f15 -> ( Statement() )*
     * f16 -> "}"
     * f17 -> "}"
     */
    @Override
    public String visit(MainClass n, String argu) throws Exception {
        current_class = n.f1.accept(this, null);

        LinkedHashMap<String, String> class_methods = new LinkedHashMap<String, String>(); 
        class_methodDetails.put(current_class, class_methods);

        System.out.println();
        
        myWriter.write("@." + current_class + "_vtable" + " = " + "global [" + methodCounter + " x i8*] []\n");

        this.vtable_methodtype_table.add_newClass(current_class);
        this.vtable_vartype_table.add_newClass(current_class);
        this.vtable_puremethtype_table.add_newClass(current_class);


        methodCounter = 0;
        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "{"
     * f3 -> ( VarDeclaration() )*
     * f4 -> ( MethodDeclaration() )*
     * f5 -> "}"
     */
    @Override
    public String visit(ClassDeclaration n, String argu) throws Exception {
        
        current_class = n.f1.accept(this, null);

        this.vtable_methodtype_table.add_newClass(current_class);
        this.vtable_vartype_table.add_newClass(current_class);
        this.vtable_puremethtype_table.add_newClass(current_class);

        LinkedHashMap<String, String> class_methods = new LinkedHashMap<String, String>(); 
        class_methodDetails.put(current_class, class_methods);

        n.f3.accept(this, null);

        n.f4.accept(this, null);

        //Now lets create the final string
            
        String final_vtableString = "@." + current_class + "_vtable" + " = " + "global [" + methodCounter + " x i8*]  [";
        
        boolean first_iter = true;

        for( Map.Entry<String,String> entry : class_methods.entrySet() ){
            
            if(first_iter){
                final_vtableString += "\n\t" + entry.getValue(); 
                first_iter=false;
            }else{
                final_vtableString += ", \n\t" + entry.getValue(); 
            }

        }

        final_vtableString += "\n]";

        System.out.println();
        
        myWriter.write("\n"+ final_vtableString + "\n");
        
        methodCounter = 0;
        System.out.println();

        return null;
    }

    /**
     * f0 -> "class"
     * f1 -> Identifier()
     * f2 -> "extends"
     * f3 -> Identifier()
     * f4 -> "{"
     * f5 -> ( VarDeclaration() )*
     * f6 -> ( MethodDeclaration() )*
     * f7 -> "}"
     */
    @Override
    public String visit(ClassExtendsDeclaration n, String argu) throws Exception {
        current_class = n.f1.accept(this, null);

        this.vtable_methodtype_table.add_newClass(current_class);
        this.vtable_vartype_table.add_newClass(current_class);
        this.vtable_puremethtype_table.add_newClass(current_class);


        LinkedHashMap<String, String> class_methods = new LinkedHashMap<String, String>(); 
        class_methodDetails.put(current_class, class_methods);

        String extended_classname = n.f3.accept(this, null);
       
        //read the variables( to add them to a variable type map ( check vtableTypes.ajava for explanation ) )
        n.f5.accept(this, null);

        //read the methods( to add them to a variable type map ( check vtableTypes.ajava for explanation ) ) 
        n.f6.accept(this, null);
        
        //add to the above maps the not overriden extended elements
        this.vtable_methodtype_table.add_extended_methInfo(current_class, extended_classname);
        this.vtable_vartype_table.add_extended_methInfo(current_class, extended_classname);
        this.vtable_puremethtype_table.add_extended_methInfo(current_class, extended_classname);


        //now we gotta add to the clang vtable the methods from the extended class too
        HashMap<String, String> extended_vtable = class_methodDetails.get(extended_classname);
        HashMap<String, String> new_vtable = class_methodDetails.get(current_class);
        


        for( Map.Entry<String,String> entry : extended_vtable.entrySet() ){
            // entry.getKey() = foo  | entry.getValue() = "i8* bitcast (i32 (i8*,i32)* @Derived.set to i8*)"
            //if has not been overriden
            if( !new_vtable.containsKey(entry.getKey()) ){
                methodCounter++;
                new_vtable.put(entry.getKey(), entry.getValue());
            }
        }

        
        
        //Now lets create the final string
        
        String final_vtableString = "@." + current_class + "_vtable" + " = " + "global [" + methodCounter + " x i8*]  [";
        
        boolean first_iter = true;

        for( Map.Entry<String,String> entry : new_vtable.entrySet() ){
            
            if(first_iter){
                final_vtableString += "\n\t" + entry.getValue(); 
                first_iter=false;
            }else{
                final_vtableString += ", \n\t" + entry.getValue(); 
            }

        }

        final_vtableString += "\n]";

        System.out.println();
        
        myWriter.write("\n"+ final_vtableString + "\n");
        
        methodCounter = 0;
        return null;
    }

    /**
     * f0 -> "public"
     * f1 -> Type()
     * f2 -> Identifier()
     * f3 -> "("
     * f4 -> ( FormalParameterList() )?
     * f5 -> ")"
     * f6 -> "{"
     * f7 -> ( VarDeclaration() )*
     * f8 -> ( Statement() )*
     * f9 -> "return"
     * f10 -> Expression()
     * f11 -> ";"
     * f12 -> "}"
     */
    @Override
    public String visit(MethodDeclaration n, String argu) throws Exception {
        String argumentList = n.f4.present() ? n.f4.accept(this, null) : "";
        
        String finalArguments = "i8*" + argumentList;


        String myType = n.f1.accept(this, null);

        if(myType.equals("int")) myType = "i32";
        else if(myType.equals("boolean")) myType = "i1";
        else if(myType.equals("int[]")) myType = "i32*";
        else myType = "i8*";


        String myName = n.f2.accept(this, null);
        

        //save it
        LinkedHashMap<String, String> tempmap = class_methodDetails.get(current_class);
        
        tempmap.put(myName, "i8* bitcast (" + myType + " (" + finalArguments +")* @" + current_class + "." + myName + " to i8*)" );

        this.vtable_methodtype_table.add_methInfo(current_class, myName,  myType + " (" + finalArguments +")*");
        this.vtable_puremethtype_table.add_methInfo(current_class, myName,  myType);



        methodCounter++;
        return null;
    }


    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    @Override
    public String visit(VarDeclaration n, String argu) throws Exception {

        this.vtable_vartype_table.add_methInfo(current_class, n.f1.accept(this, argu),  n.f0.accept(this, argu));
        
        
        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {
        String ret = ", " + n.f0.accept(this, null);

        if (n.f1 != null) {
            ret += n.f1.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    @Override
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, null);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, null);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    @Override
    public String visit(FormalParameter n, String argu) throws Exception{
        String type = n.f0.accept(this, null);
        
        
        switch(type){

            case "int":
                return "i32";
     
            case "boolean":
                return "i1";
  
            case "int[]":
                return "i32*";

            default://classname (ex. "A")
                return "i8*";
        }
        

    }

    @Override
    public String visit(ArrayType n, String argu) {
        return "int[]";
    }
    @Override
    public String visit(BooleanType n, String argu) {
        return "boolean";
    }
    @Override
    public String visit(IntegerType n, String argu) {
        return "int";
    }

    @Override
    public String visit(Identifier n, String argu) {
        return n.f0.toString();
    }
}