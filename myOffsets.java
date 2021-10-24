import java.util.*;



public class myOffsets extends symbolTable { //extend symbol table to have access to it

    HashMap<String,Pair<String,String>> saveOffsets = new HashMap<String,Pair<String,String>>();

    
    offset_table offTable = new offset_table();

    myOffsets(){

        for( String className : mydecls.table.keySet() ){
            

            //add to offset Table(this is where we save our offSets)
            offTable.add_newClass(className);

            if(className.equals(mainClassName)) continue;


            printMyOffset(className);
                      
        }

    }

    public void printMyOffset(String className){

        Integer myVarOffs = 0;
        Integer myMethOffs = 0;

        if(mydecls.check_extend(className)){ //check whether we extend 
            //if we do
            //get extended class name
            String extendsName = mydecls.get_extendClass(className);

            //get extended class last offset
            Pair<String,String> myOffs = saveOffsets.get(extendsName);


            myVarOffs = Integer.parseInt(myOffs.getElement0());
            myMethOffs = Integer.parseInt(myOffs.getElement1());


            //Now, lets save the offsets of the extended class (A) to class B | B extends A
            
            offTable.drain_extendContains(offTable.offtable.get(extendsName), offTable.offtable.get(className));


        }


        




        //Print variables
        

        // Get the variables of the class
        HashMap<String,String> myVars =  mydecls.getClassVariables(className);

        //get the variable Map in our offset table for this class
        HashMap<String,Integer> varMap = offTable.get_variableMap(className);

        for( Map.Entry<String, String> entry : myVars.entrySet() ){

            //so we dont print the overrides
            if(mydecls.checkVarΟverriding(className, entry.getKey())){ continue; }//if this method we are checkign is an override from extended class


            String myType = (String) entry.getValue();

            System.out.println(className + "." + entry.getKey() + ": " + myVarOffs);
            
            offTable.add_variableOffset(varMap, entry.getKey(), myVarOffs);

            // if((entry.getValue()).equals("int")) myVarOffs += 4; System.out.println("MPHKAAAAAAA  ");

            if(myType.equals("int") ){
                myVarOffs += 4; 
            }else if(myType.equals("boolean") ){
                myVarOffs += 1; 
            }else if(myType.equals("int[]") ){
                myVarOffs += 8; 
            }else{//its a class variable
                myVarOffs += 8; 

            }

            
            
        }

        //update final variable offset in offset map of this class(will need thsi while generating code)
        offTable.update_finalVarOffset(className, myVarOffs);
        

        //get the method offset saving table 
        // HashMap<String,Integer> methMap = offTable.get_methodMap(className);

        

        
        //get the variable Map in our offset table for this class
        HashMap<String,Integer> methMap = offTable.get_methodMap(className);



        //Print methods
        HashMap<Pair<String,String>,Vector> myMeth =  mydecls.getClassMethods(className);

        for( Pair<String,String> myMethod : myMeth.keySet() ){


            //so we dont print the overrides
            if(mydecls.checkMethodΟverriding(className, myMethod)){ continue; }//if this method we are checkign is an override from extended class

            System.out.println(className + "." + myMethod.getElement0() + ": " + myMethOffs);

            offTable.add_methodOffset(methMap, myMethod.getElement0(), myMethOffs);

            myMethOffs += 8;
            
        }



        Pair<String,String> newOffs = Pair.createPair(myVarOffs.toString(), myMethOffs.toString());


        saveOffsets.put(className, newOffs);




        
       


    }


}