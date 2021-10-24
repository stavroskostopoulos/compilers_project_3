import java.util.*;


class offset_table{

    Map<String,Vector> offtable = new HashMap<String,Vector>();


    offset_table(){
        System.out.println("Created offset table!");
    }

    public void add_newClass(String className){
        //create vector
        Vector classContains = new Vector();
        
        HashMap<String,Integer> variables = new HashMap<String,Integer>();  //name(string),offset(integer)
        HashMap<String,Integer> methods = new HashMap<String,Integer>();

        classContains.add(variables); //vector[0]
        classContains.add(methods); //vector[1]
        
        int finalVarOffset=0;

        classContains.add(finalVarOffset);


        offtable.put(className, classContains);
        
    }

    public int get_finalVarOffset(String className){
        return (int) this.offtable.get(className).get(2);
    }

    public void update_finalVarOffset(String className, int new_offset){
        this.offtable.get(className).set(2, new_offset); // call set() and replace 2 index value  
    }


    //varMaps
    public HashMap<String,Integer> get_variableMap(String className){
        return (HashMap<String,Integer>) this.offtable.get(className).get(0);
    }


    public void add_variableOffset(HashMap<String,Integer> varMap, String varName, Integer varOffset){
        varMap.put(varName, varOffset);        
    }

    //methodMaps
    public HashMap<String,Integer> get_methodMap(String className){
        return (HashMap<String,Integer>) this.offtable.get(className).get(1);
    }


    public void add_methodOffset(HashMap<String,Integer> methMap, String methName, Integer methOffset){
        methMap.put(methName, methOffset);        
    }

    public int get_method_offset(String className, String methodName){
        return get_methodMap(className).get(methodName);
    }


    //this method , copies the classContains Vector of extended_class_contains to empty_class_contains
    public void drain_extendContains(Vector extended_class_contains, Vector empty_class_contains){

        HashMap<String,Integer> varMap = (HashMap<String,Integer>) extended_class_contains.get(0);
        HashMap<String,Integer> methMap = (HashMap<String,Integer>) extended_class_contains.get(1);

        HashMap<String,Integer> destination_varMap = (HashMap<String,Integer>) empty_class_contains.get(0);
        HashMap<String,Integer> destination_methMap = (HashMap<String,Integer>) empty_class_contains.get(1);

        // copy varMap
        for( Map.Entry<String, Integer> entry : varMap.entrySet() ){
            destination_varMap.put( entry.getKey(),entry.getValue() );
        }
        
        //copy methMap
        for( Map.Entry<String, Integer> entry : methMap.entrySet() ){
            destination_methMap.put( entry.getKey(),entry.getValue() );
        }
        
    }


    //printing method for this class
    public String toString() {
        return this.offtable.toString();
    }

}