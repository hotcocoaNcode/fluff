package co.josh.bytecode.compile.fluff;

import co.josh.JoshLogger;

import java.util.*;

public class MemorySpace {
    private final int memLimit;
    public HashMap<String, Short> memoryMap = new HashMap<>();
    public HashMap<String, String> variableTypes = new HashMap<>();
    public HashMap<String, Integer> variableScopes = new HashMap<>();
    public HashMap<String, Integer> variableSizes = new HashMap<>();
    public HashMap<String, String> pointerTypeMap = new HashMap<>();

    public HashMap<Integer, Integer> blockIndices = new HashMap<>();

    public void addNew(String name, String type, Short location, Integer scope, Integer size){
        memoryMap.put(name, location);
        variableTypes.put(name, type);
        variableSizes.put(name, size);
        variableScopes.put(name, scope);
    }

    public Short mapAlloc(int size){
        short ret = 0;
        //Has to be sorted so an out of bounds doesn't accidentally set it to an in-bounds of another

        //I don't know why that's the case, but for some reason all hell would break loose the minute I named
        //the first variable in the file with anything larger than the ASCII value for "i".

        //Maybe it's just how java handles lists? No clue. At all.
        List<String> names = memoryMap.keySet().stream().sorted(Comparator.comparingInt(string -> memoryMap.get(string))).toList();
        for (int n = 0; n < names.size(); n++){
            //Get info of essentially memory.get(n)
            String s = names.get(n);
            Short c = memoryMap.get(s);
            Integer size1 = variableSizes.get(s);
            //Check every index that new object will be taking up for collision
            for (int i = 0; i < size; i++){
                // check against every block index
                for (Integer _int : blockIndices.keySet()){
                    //  ret < start || ret >= end means outside of bounds
                    if ((ret+i >= _int) && (ret+i < _int+blockIndices.get(_int))) {
                        // Set return value to upper bound
                        ret = (short) (_int+blockIndices.get(_int) - i);
                        //Restart loop
                        n = 0;
                        //Break intersection check loop
                        break;
                    }
                }
                //  check against object
                if ((ret+i >= c) && (ret+i < c+size1)) {
                    // Set return value to upper bound
                    ret = (short) (c + size1 - i);
                    //Restart loop
                    n = 0;
                    //Break intersection check loop
                    break;
                }
            }
        }
        if (ret > memLimit) {
            JoshLogger.error("Could not find space for size of " + size + "!");
        }
        return ret;
    }

    public MemorySpace(int memLimit){
        this.memLimit = memLimit;
    }


    public void free(String name){
        JoshLogger.log("Freeing \"" + name + "\" with size " + variableSizes.get(name));
        memoryMap.remove(name);
        variableSizes.remove(name);
        variableTypes.remove(name);
        variableScopes.remove(name);
    }


    Integer boc = 0; //Wrapper type for toString
    public void blockOff(int address, int size) {
        memoryMap.put(boc.toString(), (short) address);
        variableSizes.put(boc.toString(), size);
        variableScopes.put(boc.toString(), -1);
        variableTypes.put(boc.toString(), null);
    }

    public void blockOffScoped(int address, int size, int scope) {
        memoryMap.put(boc.toString(), (short) address);
        variableSizes.put(boc.toString(), size);
        variableScopes.put(boc.toString(), scope);
        variableTypes.put(boc.toString(), null);
    }
}
