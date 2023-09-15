package co.josh.compile;

import co.josh.processors.token.TokenType;

import java.util.HashMap;

public class MemorySpace {
    public HashMap<String, Short> memoryMap = new HashMap<>();
    public HashMap<String, String> variableTypes = new HashMap<>();
    public HashMap<String, Integer> variableScopes = new HashMap<>();
    public HashMap<String, Integer> variableSizes = new HashMap<>();

    public Short mapAlloc(int size){
        short ret = 0;
        for (String s : memoryMap.keySet()){
            Short c = memoryMap.get(s);
            Integer size1 = variableSizes.get(s);
            for (int i = 0; i < size; i++){
                if ((ret >= c) && (ret <= c + size1)) {
                    ret = (short) (c + size1);
                    break;
                }
            }

        }
        return ret;
    }


    public void free(String name){
        memoryMap.remove(name);
        variableSizes.remove(name);
        variableTypes.remove(name);
        variableScopes.remove(name);
    }
}
