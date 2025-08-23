package juitar.sweet_charm_o_mine.items;

import net.minecraftforge.common.IExtensibleEnum;

public enum PocketType implements IExtensibleEnum{
    COPPER(1,9,false),
    IRON(2,9,false),
    GOLD(3,9,false),
    DIAMOND(4,9,false),
    NETHERITE(5,9,false);

    private int defaultRows;
    private int defaultColumns;
    public boolean fireProof;

    PocketType(int defaultRows, int defaultColumns,boolean fireProof) {
        this.defaultRows = defaultRows;
        this.defaultColumns = defaultColumns;
        this.fireProof = fireProof;
    }
    public int getDefaultRows(){
        return this.defaultRows;
    }
    public int getDefaultColumns(){
        return this.defaultColumns;
    }
    public boolean getFireProof(){
        return this.fireProof;
    }

    public int getRows(){
        return defaultRows;
    }

    public int getColumns(){
        return defaultColumns;
    }
    public static PocketType reate(String name, int defaultRows, int defaultColumns, boolean enabled, boolean fireProof){
        throw new IllegalStateException("Enum not extended");
    }
}
