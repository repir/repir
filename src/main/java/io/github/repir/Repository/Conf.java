package io.github.repir.Repository;

/**
 *
 * @author jeroen
 */
public enum Conf {
    REPOSITORY_DIR,
    REPOSITORY_PREFIX,
    
    
    
    ;
    String label;
    
    @Override
    public String toString() {
        return label;
    }
    
    private Conf() {
        label = name().toLowerCase().replace("_", ".");
    }
    
    private Conf(String label) {
        this.label = label;
    }
   
}
