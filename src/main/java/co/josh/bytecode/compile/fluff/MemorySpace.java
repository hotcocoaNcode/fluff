package co.josh.bytecode.compile.fluff;

import co.josh.JoshLogger;

import java.util.*;

public class MemorySpace {
    private final int memLimit;
    public final HashMap<String, Short> memoryMap = new HashMap<>();
    public final HashMap<String, String> variableTypes = new HashMap<>();
    public final HashMap<String, Integer> variableScopes = new HashMap<>();
    public final HashMap<String, Integer> variableSizes = new HashMap<>();
    public final HashMap<String, String> pointerTypeMap = new HashMap<>();

    public Short mapAlloc(int size){
        short ret = 0;
        //Has to be sorted so an out of bounds doesn't accidentally set it to an in-bounds of another

        //I don't know why that's the case, but for some reason all hell would break loose the minute I named
        //the first variable in the file with anything larger than the ASCII value for "i".

        //Maybe it's just how java handles lists? No clue. At all.
        List<String> names = memoryMap.keySet().stream().sorted(Comparator.comparingInt(memoryMap::get)).toList();
        for (int n = 0; n < names.size(); n++){
            //Get info of essentially memory.get(n)
            String s = names.get(n);
            Short c = memoryMap.get(s);
            Integer size1 = variableSizes.get(s);
            //Check every index that new object will be taking up for collision
            for (int i = 0; i < size; i++){
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
        JoshLogger.log("Freeing " + variableTypes.get(name) + " (size " + variableSizes.get(name) + ") \"" + name + "\", was stored at " + memoryMap.get(name));
        memoryMap.remove(name);
        variableSizes.remove(name);
        variableTypes.remove(name);
        variableScopes.remove(name);
    }


    Integer boc = 0; //Wrapper type for toString

    public void addNew(String name, String type, Short location, Integer scope, Integer size){
        JoshLogger.log("New " + type + " (size " + size.toString() + ") \""+name+"\", storing at " + location);
        memoryMap.put(name, location);
        variableTypes.put(name, type);
        variableSizes.put(name, size);
        variableScopes.put(name, scope);
    }

    public void blockOff(int address, int size) {
        blockOffScoped(address, size, -1);
    }

    public void blockOffScoped(int address, int size, int scope) {
        JoshLogger.log("Reserving " + size + " bytes of memory at address " + address + ((scope == -1) ? "" : " with scope " + scope));
        memoryMap.put(boc.toString(), (short) address);
        variableSizes.put(boc.toString(), size);
        variableScopes.put(boc.toString(), scope);
        variableTypes.put("reserved_block", null);
        boc++;
    }
}
