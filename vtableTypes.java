import java.util.*;

//USE:
//sth like: { className = { fun1="i32 (i8*,i32)*" fun2="(i32 (i8*)*"} }

public class vtableTypes { //extend symbol table to have access to it

    HashMap<String,HashMap<String,String>> mytable = new HashMap<String,HashMap<String,String>>();

    public void add_newClass(String className){

        HashMap<String,String> new_methMap = new HashMap<String,String>();

        this.mytable.put(className, new_methMap);

    }

    public void add_methInfo(String className, String methName, String methInfo){
        this.mytable.get(className).put(methName, methInfo);
    }

    public void add_extended_methInfo(String className, String extended_classname){
        HashMap<String,String> tempmap = (HashMap<String,String>) this.mytable.get(className);
        
        for( Map.Entry<String, String> entry : this.mytable.get(extended_classname).entrySet() ){     
            //if is has not been overriden
            if(!tempmap.containsKey(entry.getKey())) add_methInfo(className, entry.getKey(), entry.getValue());
        }
    }

    public String get_methInfo(String className, String methName){
        return this.mytable.get(className).get(methName);
    }


    //printing method for this class
    public String toString() {
        return this.mytable.toString();
    }
}