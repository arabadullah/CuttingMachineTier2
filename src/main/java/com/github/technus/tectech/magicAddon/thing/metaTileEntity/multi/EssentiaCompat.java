package com.github.technus.tectech.magicAddon.thing.metaTileEntity.multi;

import com.github.technus.tectech.elementalMatter.classes.cElementalInstanceStack;
import com.github.technus.tectech.thing.metaTileEntity.multi.GT_MetaTileEntity_MultiblockBase_EM;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by Tec on 21.05.2017.
 */
public class EssentiaCompat {
    public static EssentiaCompat essentiaContainerCompat;

    public void run(){}

    public boolean check(GT_MetaTileEntity_MultiblockBase_EM meta){
        return false;
    }

    public TileEntity getContainer(GT_MetaTileEntity_MultiblockBase_EM meta){
        return null;
    }

    public boolean putElementalInstanceStack(TileEntity conatainer, cElementalInstanceStack stack){
        return false;
    }

    public cElementalInstanceStack getFromContainer(TileEntity container){
        return null;
    }
}
