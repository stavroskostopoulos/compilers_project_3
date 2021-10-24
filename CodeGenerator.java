import syntaxtree.*;
import java.io.FileWriter;   // Import the FileWriter class
import java.util.*;




class CodeGenerator extends myOffsets {

    String current_class = null; //this will hold the current class name

    int variable_name_counter = -1;
    int if_label_counter = -1;
    int short_circ_counter = -1;
    int arrsize_label_counter = -1;
    int oob_label_counter = -1;
    int while_label_counter = -1;


    
    Vector<String> meth_call_arguments  = new Vector<String>();

    HashMap<String,String> automatic_vars = new HashMap<String,String>();


    vtableTypes vtable_methodtype_table = null;
    vtableTypes vtable_vartype_table = null;
    vtableTypes vtable_puremethtype_table = null;



    FileWriter myWriter = null;
    
    CodeGenerator(FileWriter writer, vtableTypes  methtypeTable, vtableTypes  vartypeTable, vtableTypes  puremethtypeTable){
        myWriter=writer;
        vtable_methodtype_table = methtypeTable;
        vtable_vartype_table = vartypeTable;
        vtable_puremethtype_table = puremethtypeTable;

    }

    public String get_new_automatic_variable(){
        return "%_" + (++variable_name_counter);
    }

    public String get_current_automatic_variable(){
        return "%_" + variable_name_counter;
    }

    public String get_previous_automatic_variable(){
        return "%_" + (variable_name_counter-1);
    }

    public int get_vtable_size(String className){
        HashMap<String,Integer> methMap = offTable.get_methodMap(className);
        return methMap.size();
    }


    public void load_automatic_variable(String type, String varName) throws Exception{
        this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load " + type + ", " + type +"* %" + varName);
    }

    public static boolean isNumeric(String string) {
        
            
       
            
        if(string == null || string.equals("")) {
            return false;
        }
        
        try {
            Integer.parseInt(string);;
            
            return true;
        } catch (NumberFormatException e) {
            System.out.println("Input String cannot be parsed to Integer.");
        }
        return false;
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

        

        this.myWriter.write("define i32 @main() {");


        n.f14.accept(this, null);
        n.f15.accept(this, null);
        

        this.myWriter.write("\n\n\tret i32 0\n}\n");

        variable_name_counter = -1;
        if_label_counter = -1;
        short_circ_counter = -1;
        arrsize_label_counter = -1;
        oob_label_counter = -1;
        while_label_counter = -1;
        automatic_vars.clear();
        System.out.println();

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

        n.f4.accept(this, null);

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

        n.f6.accept(this, null);

        System.out.println();

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
       ;
        
        String finalArgumentsString = n.f4.present() ? n.f4.accept(this, "param_list") : "";
        
        //if there are no argumetns
        if(!n.f4.present()) finalArgumentsString = "i8* %this";

        String myType = n.f1.accept(this, null);
        String myName = n.f2.accept(this, null);

        if(myType.equals("int")) myType = "i32";
        else if(myType.equals("boolean")) myType = "i1";
        else if(myType.equals("int[]")) myType = "i32*";
        else myType = "i8*";
        
        this.myWriter.write("\ndefine " + myType + " @" + this.current_class + "." + myName + "(" + finalArgumentsString + ") {");

        n.f4.accept(this, "param_alloc");
        n.f7.accept(this, null);
        n.f8.accept(this, null);

        
        //return
        this.myWriter.write("\n\n");
        String return_var = n.f10.accept(this, "assignment");
        this.myWriter.write("\n\n\tret " + myType + " " + return_var);
        
        
        this.myWriter.write("\n}\n");

        variable_name_counter = -1;
        if_label_counter = -1;
        short_circ_counter = -1;
        arrsize_label_counter = -1;
        oob_label_counter = -1;
        while_label_counter = -1;
        automatic_vars.clear();

        

        return null;
    }


    /**
    * f0 -> Type()
    * f1 -> Identifier()
    * f2 -> ";"
    */
    public String visit(VarDeclaration n, String argu) throws Exception {
        
        
        String myType = n.f0.accept(this, null);
        String myName = n.f1.accept(this, null);


        automatic_vars.put(myName, myType);
        

        if(myType.equals("int")) myType = "i32";
        else if(myType.equals("boolean")) myType = "i1";
        else if(myType.equals("int[]")) myType = "i32*";
        else myType = "i8*";

        myWriter.write("\n\t%" + myName + " = alloca " + myType + "\n");

        return null;
    }





    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */

    // this has 2 modes
    // 1) argu == param_list -> when we want to print sth like i8* %this, i32 %.x
    // 2) argu == param_alloc -> when we want to print the allocations for each param
    @Override
    public String visit(FormalParameterList n, String argu) throws Exception {

        if(argu.equals("param_list")){
            
            String finalString = "i8* %this, ";

            finalString += n.f0.accept(this, argu);

            if (n.f1 != null) {
                finalString += n.f1.accept(this, argu);
            }

            return finalString;

        }else if(argu.equals("param_alloc")){

            n.f0.accept(this, argu);

            if (n.f1 != null) {
                n.f1.accept(this, argu);
            }

            return null;

        }

        

        return null;
    }

    /**
     * f0 -> FormalParameter()
     * f1 -> FormalParameterTail()
     */
    public String visit(FormalParameterTerm n, String argu) throws Exception {
        return n.f1.accept(this, argu);
    }

    /**
     * f0 -> ","
     * f1 -> FormalParameter()
     */
    @Override
    public String visit(FormalParameterTail n, String argu) throws Exception {
        String ret = "";
        for ( Node node: n.f0.nodes) {
            ret += ", " + node.accept(this, argu);
        }

        return ret;
    }

    /**
     * f0 -> Type()
     * f1 -> Identifier()
     */
    // this has 2 modes
    // 1) argu == param_list -> when we want to print sth like i8* %this, i32 %.x
    // 2) argu == param_alloc -> when we want to print the allocations for each param


    @Override
    public String visit(FormalParameter n, String argu) throws Exception{

        String type = n.f0.accept(this, null);
        String javaType = type;

        if(type.equals("int")) type = "i32";
        else if(type.equals("boolean")) type = "i1";
        else if(type.equals("int[]")) type = "i32*";
        else type = "i8*";


        String name = n.f1.accept(this, null);


        if(argu.equals("param_list")){        

            automatic_vars.put(name,javaType);

            return type + " %." + name;

        }else if(argu.equals("param_alloc")){

            this.myWriter.write("\n\t%" + name + " = alloca " + type);
            this.myWriter.write("\n\tstore " + type + " %." + name + ", " + type + "* %" + name + "\n");

            
        }

        return null;
        
    }



    /**
    * f0 -> Expression()
    * f1 -> ExpressionTail()
    */
    public String visit(ExpressionList n, String argu) throws Exception {
        
        String expr = n.f0.accept(this, argu);
        if(expr!=null){
            meth_call_arguments.add(expr);
        }
        n.f1.accept(this, argu);

        
        return null;
    }

    /**
     * f0 -> ( ExpressionTerm() )*
    */
    public String visit(ExpressionTail n, String argu) throws Exception {
        n.f0.accept(this, argu);
        return null;
    }

    /**
     * f0 -> ","
    * f1 -> Expression()
    */
    public String visit(ExpressionTerm n, String argu) throws Exception {
        String expr = n.f1.accept(this, "argument");
        
        
       
        meth_call_arguments.add(expr);
         
        return ", " + expr;
    }








    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> Identifier()
    * f3 -> "("
    * f4 -> ( ExpressionList() )?
    * f5 -> ")"
    */
    public String visit(MessageSend n, String argu) throws Exception {
        
        if(argu==null || argu.equals("assignment") || argu.equals("pureType")  || argu.equals("msg")){
            // "this" or class_name

            String className = n.f0.accept(this, "msg");
            //if this was a this expression,we got a className back if not, we got an Object variable name,and we have to find the class name
            //then this was an allocated class variable 
            if(automatic_vars.containsKey(className)){
                
                String obj_varName = className;
                
                className = automatic_vars.get(obj_varName);

                //function name
                String funcName = n.f2.accept(this, null);

                String pure_type = vtable_puremethtype_table.get_methInfo(className, funcName);
                if(argu.equals("pureType")){
                    return pure_type;
                }


                //first load the object pointer
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i8*, i8** %" + obj_varName);
                
                String receiver_object = get_current_automatic_variable();
                
                //do bitcasts
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = bitcast i8* " + get_previous_automatic_variable() + " to i8***");
                //load vtable pointer
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i8**, i8*** " + get_previous_automatic_variable());
                

                

                //get pointer to the index of the vtable (index is the offset from the 2nd project of the course)
                
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = getelementptr i8*, i8** " + get_previous_automatic_variable() + ", i32 " + (offTable.get_method_offset(className, funcName)/8));

                //get actual func pointer
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i8*, i8** " + get_previous_automatic_variable());

                //cast the function pointer 
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = bitcast i8* " + get_previous_automatic_variable() + " to " + vtable_methodtype_table.get_methInfo(className, funcName));
                String func_pointer = get_current_automatic_variable();

                
                n.f4.accept(this, "argument");
                String call_arguments = null;
                
                Iterator<String> value = meth_call_arguments.iterator();
                int arg_flag = 0;
                
                while(value.hasNext()){
                    if(arg_flag==0){
                        call_arguments = value.next();
                        arg_flag++;
                        continue;
                    }
                    call_arguments += ", "+ value.next();
                    arg_flag++;

                }

                String methCall = null;

                if(call_arguments==null) methCall =  func_pointer + "(i8* " + receiver_object + ")\n";
                else methCall =  func_pointer + "(i8* " + receiver_object + ", " + call_arguments + ")\n";
                
                
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = call "+ pure_type + " "+ methCall);       
                
                meth_call_arguments.clear();

                if(argu.equals("argument")){
                    return vtable_methodtype_table.get_methInfo(className, funcName) + get_current_automatic_variable();
                }

                return get_current_automatic_variable();

            }else if(className.equals("this")){//EIAMI APO THIS EXPRESSION ara...

                String classType = n.f0.accept(this, "classtype");

                

                //function name
                String funcName = n.f2.accept(this, null);

                String pure_type = vtable_puremethtype_table.get_methInfo(classType, funcName);
                if(argu.equals("pureType")){
                    return pure_type;
                }

                //do bitcasts
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = bitcast i8* %this to i8***");
                //load vtable pointer
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i8**, i8*** " + get_previous_automatic_variable());
                

                

                //get pointer to the index of the vtable (index is the offset from the 2nd project of the course)
                
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = getelementptr i8*, i8** " + get_previous_automatic_variable() + ", i32 " + (offTable.get_method_offset(classType, funcName)/8));

                //get actual func pointer
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i8*, i8** " + get_previous_automatic_variable());

                //cast the function pointer 
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = bitcast i8* " + get_previous_automatic_variable() + " to " + vtable_methodtype_table.get_methInfo(classType, funcName));
                String func_pointer = get_current_automatic_variable();
                
                
                
            
                n.f4.accept(this, "argument");
                String call_arguments = null;

                Iterator<String> value = meth_call_arguments.iterator();

                int arg_flag = 0;
                while(value.hasNext()){
                    if(arg_flag==0){
                        call_arguments = value.next();
                        arg_flag++;
                        continue;
                    }

                    call_arguments += ", "+ value.next();
                    arg_flag++;

                }
                
                String methCall = null;

                if(call_arguments==null) methCall =  func_pointer + "(i8* %this)\n";
                else methCall =  func_pointer + "(i8* %this, " + call_arguments + ")\n";
                
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = call "+ pure_type + " "+ methCall);       
                
                meth_call_arguments.clear();

                if(argu.equals("argument")){
                    return vtable_methodtype_table.get_methInfo(classType, funcName) + get_current_automatic_variable();
                }


                return get_current_automatic_variable();



            }else{//it was an allocation expressiona nd we got back the name of the object (ex. Base)
                String allocation_var = className;
                String classType = n.f0.accept(this, "classtype");
                
                //function name
                String funcName = n.f2.accept(this, null);

                String pure_type = vtable_puremethtype_table.get_methInfo(classType, funcName);
                if(argu.equals("pureType")){
                    return pure_type;
                }

                //do bitcasts
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = bitcast i8* " + allocation_var +" to i8***");
                //load vtable pointer
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i8**, i8*** " + get_previous_automatic_variable());


                

                //get pointer to the index of the vtable (index is the offset from the 2nd project of the course)

                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = getelementptr i8*, i8** " + get_previous_automatic_variable() + ", i32 " + (offTable.get_method_offset(classType, funcName)/8));

                //get actual func pointer
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i8*, i8** " + get_previous_automatic_variable());

                //cast the function pointer 
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = bitcast i8* " + get_previous_automatic_variable() + " to " + vtable_methodtype_table.get_methInfo(classType, funcName));
                String func_pointer = get_current_automatic_variable();
                


                n.f4.accept(this, "argument");
                String call_arguments = null;

                Iterator<String> value = meth_call_arguments.iterator();
                
                int arg_flag = 0;
                while(value.hasNext()){
                    if(arg_flag==0){
                        call_arguments = value.next();
                        arg_flag++;
                        continue;
                    }
                    call_arguments += ", "+ value.next();
                    arg_flag++;
                }

                
                
                String methCall = null;

                if(call_arguments==null) methCall =  func_pointer + "(i8* " + allocation_var + ")\n";
                else methCall =  func_pointer + "(i8* " + allocation_var + ", " + call_arguments + ")\n";
                
                
                
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = call " + pure_type +" "+ methCall);       

                meth_call_arguments.clear();

                if(argu.equals("argument")){
                    return vtable_methodtype_table.get_methInfo(classType, funcName) + get_current_automatic_variable();
                }

                
                return get_current_automatic_variable();



            }
        }else{
            return null;
        }


    }


    /**
    * f0 -> Identifier()
    * f1 -> "["
    * f2 -> Expression()
    * f3 -> "]"
    * f4 -> "="
    * f5 -> Expression()
    * f6 -> ";"
    */
    public String visit(ArrayAssignmentStatement n, String argu) throws Exception {
            int oob_counter = ++oob_label_counter;


            String name = n.f0.accept(this, "tobe_assigned");
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i32*, i32** " + name);
            String array_pointer = get_current_automatic_variable();
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i32, i32* " + get_previous_automatic_variable());
            String array_size = get_current_automatic_variable();


            String index = n.f2.accept(this, "assignment");

            //check index is greater than zero
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = icmp sge i32 " + index +", 0");
            String cond1 = get_current_automatic_variable();
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = icmp slt i32 " + index + ", " + array_size);
            String cond2 = get_current_automatic_variable();

            //check if both are okay
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = and i1 " + cond1 + ", " + cond2 );
            this.myWriter.write("\n\tbr i1 " + get_current_automatic_variable() + ", label %oob_ok_"+ oob_counter + ", label %oob_err_" + oob_counter + "\n\n");

            //label error
            this.myWriter.write("\n\toob_err_" + oob_counter + ":");
            this.myWriter.write("\n\tcall void @throw_oob()");
            this.myWriter.write("\n\tbr label %oob_ok_" + oob_counter + "\n");

            //label ok
            this.myWriter.write("\n\toob_ok_" + oob_counter + ":");
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = add i32 1, " + index);
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = getelementptr i32, i32* " + array_pointer + ", i32 " + get_previous_automatic_variable());
            String array_ptr = get_current_automatic_variable();

            String expr_result = n.f5.accept(this, "assignment");

            this.myWriter.write("\n\tstore i32 " + expr_result + ", i32* " + array_ptr + "\n\n");

            return null;
    }


    /**
    * f0 -> Identifier()
    * f1 -> "="
    * f2 -> Expression()
    * f3 -> ";"
    */
    public String visit(AssignmentStatement n, String argu) throws Exception {
    
        String varName = n.f0.accept(this, "tobe_assigned");
        
        
        String pureType = n.f2.accept(this, "pureType"); //if expression is an identifier,get pureType -> i32 | i32* etc.
        
        // myWriter.write("ASSIGNMENT RE " + varName+ " exw edw "+ value + " kai type "+ pureType + "\n");
        
        
            
        String return_var = n.f2.accept(this, "assignment"); 

        this.myWriter.write("\n\tstore " + pureType + " " + return_var + ", " + pureType + "* " + varName);
        
        
        return null;
    }


    /**
    * f0 -> "new"
    * f1 -> Identifier()
    * f2 -> "("
    * f3 -> ")"
    */
    public String visit(AllocationExpression n, String argu) throws Exception {
        
        //Print the sequence of instructions to create a new Identifier() object!
        if(argu==null){
            return null;
            
        }else if(argu.equals("msg") || argu.equals("assignment") || argu.equals("classtype") || argu.equals("argument")){
            String objectName = n.f1.accept(this, null);

            if(argu.equals("classtype")){
                return objectName;
            }

            //calloc
            int bytes2alloc = offTable.get_finalVarOffset(objectName) + 8;
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = call i8* @calloc(i32 1, i32 " + bytes2alloc + ")");

            String calloc_var = get_current_automatic_variable();

            //bitcast
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = bitcast i8* " + get_previous_automatic_variable() + " to i8***");

            //GEP
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = getelementptr [" + get_vtable_size(objectName) + " x i8*], [" + get_vtable_size(objectName) + " x i8*]* @." + objectName + "_vtable, i32 0, i32 0");

            //set v_table to correct address

            this.myWriter.write("\n\tstore i8** " + get_current_automatic_variable() + ", i8*** " + get_previous_automatic_variable());

            //store the address of the new object on the stack
            // this.myWriter.write("\n\tstore i8* " + calloc_var + ", i8** ");

            if(argu.equals("argument")){
                return "i8*" + calloc_var;
            }

            return calloc_var;
        }
    

        //if we just need to return the type
        else if(argu.equals("type")) return "i8*";
        else if(argu.equals("pureType")) return "i8*";
        
        return null;
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "."
    * f2 -> "length"
    */
    public String visit(ArrayLength n, String argu) throws Exception {
        if(argu==null){
            String expr_result = n.f0.accept(this, "assignment");
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i32, i32* " + expr_result);

        }else if(argu.equals("pureType")) {
            return "i32";
        }

        String expr_result = n.f0.accept(this, "assignment");
        this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i32, i32* " + expr_result);

        return get_current_automatic_variable();
    }


    /**
    * f0 -> "if"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    * f5 -> "else"
    * f6 -> Statement()
    */
    public String visit(IfStatement n, String argu) throws Exception {
        int if_counter = ++if_label_counter;
        
        String statement_result = n.f2.accept(this, "ifstate");
        


        this.myWriter.write("\n\tbr i1 " + statement_result + ", label %if_then_" + if_counter + ", label %if_else_" + if_counter + "\n");

        this.myWriter.write("\n\tif_else_"+ if_counter+":");
        n.f6.accept(this, argu);
        this.myWriter.write("\n\tbr label %if_end_" + if_counter + "\n\n");

        this.myWriter.write("\n\tif_then_"+ if_counter+":");
        n.f4.accept(this, argu);
        this.myWriter.write("\n\tbr label %if_end_" + if_counter);
        

        this.myWriter.write("\n\tif_end_"+ if_counter+":");

        
        return null;
    }
    
    /**
    * f0 -> "System.out.println"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> ";"
    */
    public String visit(PrintStatement n, String argu) throws Exception {
        
        //The f2 could be:
        //  1)STRING LITERAL (variable name)
        //  2)INTEGER LITERAL (int value)
        //  3)INT TYPE FUNCTION

        String expr_result = n.f2.accept(this, "assignment");
       
        
        this.myWriter.write("\n\t" + "call void (i32) @print_int(i32 " + expr_result + ")\n");


        return null;
    }


    /**
    * f0 -> PrimaryExpression()
    * f1 -> "+"
    * f2 -> PrimaryExpression()
    */
    public String visit(PlusExpression n, String argu) throws Exception {
        
        if(argu==null){
            return null;
        }else if(argu.equals("assignment") || argu.equals("argument")){

            String expr1 = n.f0.accept(this, "assignment");

            String expr2 = n.f2.accept(this, "assignment");

            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = add i32 " + expr1 + ", " + expr2);
        
            if(argu.equals("argument")){

                return "i32 " + get_current_automatic_variable();
            } 

            return get_current_automatic_variable();
        }else if(argu.equals("pureType")){
            return "i32";
        }

        return null;

        
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "*"
    * f2 -> PrimaryExpression()
    */
    public String visit(TimesExpression n, String argu) throws Exception {
        
        if(argu==null){
            return null;
        }else if(argu.equals("assignment") || argu.equals("argument")){

            String expr1 = n.f0.accept(this, "assignment");

            String expr2 = n.f2.accept(this, "assignment");

            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = mul i32 " + expr1 + ", " + expr2);
        
            if(argu.equals("argument")){

                return "i32 " + get_current_automatic_variable();
            } 

            return get_current_automatic_variable();
        }else if(argu.equals("pureType")){
            return "i32";
        }

        return null;

        
    }


    /**
    * f0 -> PrimaryExpression()
    * f1 -> "-"
    * f2 -> PrimaryExpression()
    */
    public String visit(MinusExpression n, String argu) throws Exception {
        if(argu==null){
            return null;
        }else if(argu.equals("assignment") || argu.equals("argument")){

            String expr1 = n.f0.accept(this, "assignment");

            String expr2 = n.f2.accept(this, "assignment");

            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = sub i32 " + expr1 + ", " + expr2);
            
            if(argu.equals("argument")){

                return "i32 " + get_current_automatic_variable();
            } 

            return get_current_automatic_variable();
        }else if(argu.equals("pureType")){
            return "i32";
        }

        return null;
    }


    /**
    * f0 -> PrimaryExpression()
    * f1 -> "&&"
    * f2 -> PrimaryExpression()
    */
    public String visit(AndExpression n, String argu) throws Exception {
        
        if(argu==null){
            return null;
        }else if(argu.equals("assignment") || argu.equals("argument")){

            String expr1 = n.f0.accept(this, "assignment");

            String expr2 = n.f2.accept(this, "assignment");

            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = and i1 " + expr1 + ", " + expr2);
        
            if(argu.equals("argument")){

                return "i1 " + get_current_automatic_variable();
            } 

            return get_current_automatic_variable();
        }else if(argu.equals("ifstate")){
            int and_counter = ++short_circ_counter;

            int label_expr0 = and_counter;
            int label_expr1 = and_counter+1;
            int label_expr2 = and_counter+2;
            int label_expr3 = and_counter+3;



            String expr1 = n.f0.accept(this, "assignment");

            

            this.myWriter.write("\n\tbr i1 " + expr1 + ", label %expr_res_" + label_expr1 + ", label %expr_res_" + label_expr0 + "\n");
            
            this.myWriter.write("\n\texpr_res_" + label_expr0 +":");
            this.myWriter.write("\n\tbr label %expr_res_" + label_expr3 + "\n");

            this.myWriter.write("\n\texpr_res_" + label_expr1 +":");
            String expr2 = n.f2.accept(this, "assignment");
            this.myWriter.write("\n\tbr label %expr_res_" + label_expr2 + "\n");

            this.myWriter.write("\n\texpr_res_" + label_expr2 +":");
            this.myWriter.write("\n\tbr label %expr_res_" + label_expr3 + "\n");

            this.myWriter.write("\n\texpr_res_" + label_expr3 +":");
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = phi i1 [ 0, %expr_res_" + label_expr0 + " ], [ " + expr2 + ", %expr_res_" + label_expr2  +" ] ");

            return get_current_automatic_variable();
            

        }else if(argu.equals("pureType")){
            return "i1";
        }

        return null;
    }

    /**
    * f0 -> "while"
    * f1 -> "("
    * f2 -> Expression()
    * f3 -> ")"
    * f4 -> Statement()
    */
    public String visit(WhileStatement n, String argu) throws Exception {
        
        int loop0 = ++while_label_counter;
        int loop1 = ++while_label_counter;
        int loop2 = ++while_label_counter;


        this.myWriter.write("\n\tbr label %loop" + loop0);
        this.myWriter.write("\n\tloop" + loop0 + ":");

        String expr_result = n.f2.accept(this, "ifstate");
        
        this.myWriter.write("\n\tbr i1 " + expr_result + ", label %loop" + loop1 + ", label %loop" + loop2 + "\n\n");


        this.myWriter.write("\n\tloop" + loop1 + ":");
        n.f4.accept(this, argu);
        
        this.myWriter.write("\n\tbr label %loop" + loop0 + "\n\n");

        this.myWriter.write("\n\tloop" + loop2 + ":");

        
        
        return null;
    }



    /**
    * f0 -> PrimaryExpression()
    * f1 -> "<"
    * f2 -> PrimaryExpression()
    */
    public String visit(CompareExpression n, String argu) throws Exception {
        
        if(argu==null){
            return null;
        }else if(argu.equals("assignment") || argu.equals("argument")){

            String expr1 = n.f0.accept(this, "assignment");

            String expr2 = n.f2.accept(this, "assignment");

            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = icmp slt i32 " + expr1 + ", " + expr2);
        
            if(argu.equals("argument")){
                return "i1 " + get_current_automatic_variable();
            } 

            return get_current_automatic_variable();
        }else if(argu.equals("ifstate")){
            String expr1 = n.f0.accept(this, "assignment");

            String expr2 = n.f2.accept(this, "assignment");

            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = icmp slt i32 " + expr1 + ", " + expr2);
        
            return get_current_automatic_variable();
        }else if(argu.equals("pureType")){
            return "i1";
        }

        return null;
    }

    /**
    * f0 -> "!"
    * f1 -> PrimaryExpression()
    */
    public String visit(NotExpression n, String argu) throws Exception {
        
        if(argu == null){
            String expr_result = n.f1.accept(this, "assignment");
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = xor i1 " + expr_result + ", 1" );
        
            return get_current_automatic_variable();
        
        }

        String expr_result = n.f1.accept(this, "assignment");
        this.myWriter.write("\n\t" + get_new_automatic_variable() + " = xor i1 " + expr_result + ", 1" );
        
        if(argu.equals("argument")){
            return "i1 " + get_current_automatic_variable();
        } 

        return get_current_automatic_variable();

    }

    @Override
    public String visit(ArrayType n, String argu) {
        return "int[]";
    }

    public String visit(BooleanType n, String argu) {
        return "boolean";
    }

    public String visit(IntegerType n, String argu) {
        return "int";
    }

    /**
    * f0 -> "this"
    */
    public String visit(ThisExpression n, String argu) throws Exception {
        if(argu==null){
            return "this";
        }else if(argu.equals("classtype")){
            return current_class;
        }else if(argu.equals("assignment")){
            return "%this";
        }else if(argu.equals("pureType")){
            return "i8*";
        }
        return "this";
    }

    /**
    * f0 -> "new"
    * f1 -> "int"
    * f2 -> "["
    * f3 -> Expression()
    * f4 -> "]"
    */
    public String visit(ArrayAllocationExpression n, String argu) throws Exception {
        if(argu == null){
            return "i32*";
        }else if(argu.equals("assignment")  || argu.equals("argument")){

        
        
            int size_label = ++arrsize_label_counter;

            String expr_result = n.f3.accept(this, argu);
            
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = add i32 1, " + expr_result);
            String array_size = get_current_automatic_variable();

            //check size
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = icmp sge i32 " + get_previous_automatic_variable() + ", 1" );
            this.myWriter.write("\n\tbr i1 " + get_current_automatic_variable() + ", label %nsz_ok_" + size_label + ", label %nsz_err_" + size_label + "\n");

            //if it was negative
            this.myWriter.write("\n\tnsz_err_" + size_label + ":");
            this.myWriter.write("\n\tcall void @throw_nsz()");
            this.myWriter.write("\n\tbr label %nsz_ok_" + size_label+"\n\n");

            //all ok.let's allocate
            this.myWriter.write("\n\tnsz_ok_" + size_label + ":");
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = call i8* @calloc(i32 " + array_size + ", i32 4)\n");
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = bitcast i8* " + get_previous_automatic_variable() + " to i32*");
            
            //store array size in the first position of the array
            this.myWriter.write("\n\tstore i32 " + expr_result + ", i32* " + get_current_automatic_variable());
            this.myWriter.write("\n");
            
            if(argu.equals("argument")){
                return "i32*" + get_current_automatic_variable();
            }
            
            
            return get_current_automatic_variable();

        }else if(argu.equals("pureType") || argu.equals("type")){
            return "i32*";    
        }

        return null;
    }


    /**
    * f0 -> "("
    * f1 -> Expression()
    * f2 -> ")"
    */
    public String visit(BracketExpression n, String argu) throws Exception {
        
        String result = n.f1.accept(this, argu);
        return result;
    }

    public String visit(TrueLiteral n, String argu) throws Exception {

        if(argu == null){
            n.f0.accept(this, argu);
            return "1";
        }else if(argu.equals("type") || argu.equals("pureType")){
            return "i1";
        }else if(argu.equals("assignment")){
            
            return "1";
        }else if(argu.equals("argument")){
            return "i1 1";
        }else if(argu.equals("value") || argu.equals("ifstate")){
            return "1";
        }
        
        return "1";
        
    }

    public String visit(FalseLiteral n, String argu) throws Exception {
        
        if(argu == null){
            n.f0.accept(this, argu);
            return "0";
        }else if(argu.equals("type") || argu.equals("pureType")){
            return "i1";
        }else if(argu.equals("assignment")){     
            return "0";
        }else if(argu.equals("argument")){
            return "i1 0";
        }else if(argu.equals("value") || argu.equals("ifstate")){
            return "0";
        }
        
        return "0";
    }

    /**
    * f0 -> PrimaryExpression()
    * f1 -> "["
    * f2 -> PrimaryExpression()
    * f3 -> "]"
    */
    public String visit(ArrayLookup n, String argu) throws Exception {
        
        if(argu==null){
            return "i32";
        }else if(argu.equals("assignment")){

        
            int oob_counter = ++oob_label_counter;


            String name = n.f0.accept(this, "tobe_assigned");
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i32*, i32** " + name);
            String array_pointer = get_current_automatic_variable();
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i32, i32* " + get_previous_automatic_variable());
            String array_size = get_current_automatic_variable();


            String index = n.f2.accept(this, "assignment");

            //check index is greater than zero
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = icmp sge i32 " + index +", 0");
            String cond1 = get_current_automatic_variable();
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = icmp slt i32 " + index + ", " + array_size);
            String cond2 = get_current_automatic_variable();

            //check if botha re okay
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = and i1 " + cond1 + ", " + cond2 );
            this.myWriter.write("\n\tbr i1 " + get_current_automatic_variable() + ", label %oob_ok_"+ oob_counter + ", label %oob_err_" + oob_counter + "\n\n");

            //label error
            this.myWriter.write("\n\toob_err_" + oob_counter + ":");
            this.myWriter.write("\n\tcall void @throw_oob()");
            this.myWriter.write("\n\tbr label %oob_ok_" + oob_counter + "\n\n");

            //label ok
            this.myWriter.write("\n\toob_ok_" + oob_counter + ":");
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = add i32 1, " + index);
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = getelementptr i32, i32* " + array_pointer + ", i32 " + get_previous_automatic_variable());
            this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load i32, i32* " + get_previous_automatic_variable());

            return get_current_automatic_variable();
        }else if(argu.equals("pureType") || argu.equals("type")){
            return "i32";
        }

        return null;
        
    }



    /**
    * f0 -> <INTEGER_LITERAL>
    */
    public String visit(IntegerLiteral n, String argu) throws Exception {
        
        if(argu == null) return n.f0.toString();
        if(argu.equals("type")) return "i32";
        if(argu.equals("pureType")) return "i32";
        if(argu.equals("assignment")) return n.f0.toString();

        String myValue = n.f0.toString();

        if(argu.equals("argument")){

            return "i32 " + myValue;
        } 

        return myValue;
        
    }

    @Override
    public String visit(Identifier n, String argu) throws Exception {
        
        if(argu == null || argu.equals("msg")){
           
            return  n.f0.toString();

        }else if(argu.equals("pureType")){
            
            String varName = n.f0.toString();

            //check whether its automatic variable or class field 
                        
            //if its class field
            if(offTable.get_variableMap(current_class).containsKey(varName)){

                String myType = vtable_vartype_table.get_methInfo(current_class, varName);

                if(myType.equals("int"))    return "i32";
                else if(myType.equals("int[]"))    return "i32*";
                else if(myType.equals("boolean"))    return "i1";
                else return "i8*";


            }else{ // its an automatic variable

                String myType = automatic_vars.get(varName);
                
                
                if(myType.equals("int"))    return "i32";
                else if(myType.equals("int[]"))    return "i32*";
                else if(myType.equals("boolean"))    return "i1";
                else return "i8*";

            }

        }else if(argu.equals("assignment") || argu.equals("ifstate") || argu.equals("argument") ){

            String varName = n.f0.toString();
            
            //check whether its automatic variable or class field 
            
            //if its class field
            if(offTable.get_variableMap(current_class).containsKey(varName)){

                String myType = vtable_vartype_table.get_methInfo(current_class, varName);
                String varType = null;

                if(myType.equals("int"))    varType = "i32";
                else if(myType.equals("int[]"))    varType = "i32*";
                else if(myType.equals("boolean"))    varType = "i1";
                else varType = "i8*";


                int varOffset = offTable.get_variableMap(current_class).get(varName) + 8;
                
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = getelementptr i8, i8* %this, i32 " + varOffset);
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = bitcast i8* " + get_previous_automatic_variable() + " to " + varType + "*");
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load " + varType + ", " + varType + "* " + get_previous_automatic_variable());
                   

                if(argu.equals("argument")){
                    return varType + get_current_automatic_variable();
                }

                return get_current_automatic_variable();

            }else{ // its an automatic variable

                String myType = automatic_vars.get(varName);
                String varType = null;
                
                if(myType.equals("int"))    varType = "i32";
                else if(myType.equals("int[]"))    varType = "i32*";
                else if(myType.equals("boolean"))    varType = "i1";
                else varType = "i8*";


                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = load " + varType + " , " + varType + "* %" + varName);

                if(argu.equals("argument")){
                    return varType + get_current_automatic_variable();
                }

                return get_current_automatic_variable();
            }

        }else if(argu.equals("tobe_assigned")){
            String varName = n.f0.toString();


            //if its class field
            if(offTable.get_variableMap(current_class).containsKey(varName)){
                String myType = vtable_vartype_table.get_methInfo(current_class, varName);
                String varType = null;

                if(myType.equals("int"))    varType = "i32";
                else if(myType.equals("int[]"))    varType = "i32*";
                else if(myType.equals("boolean"))    varType = "i1";
                else varType = "i8*";


                int varOffset = offTable.get_variableMap(current_class).get(varName) + 8;
                
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = getelementptr i8, i8* %this, i32 " + varOffset);
                this.myWriter.write("\n\t" + get_new_automatic_variable() + " = bitcast i8* " + get_previous_automatic_variable() + " to " + varType + "*");
            
                return get_current_automatic_variable();
            }else{
                return "%" + n.f0.toString();
            }


        }


        return null;
    }

    
}