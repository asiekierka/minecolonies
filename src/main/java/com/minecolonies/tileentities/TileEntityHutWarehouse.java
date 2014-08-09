package com.minecolonies.tileentities;

import com.minecolonies.entity.EntityCitizen;

public class TileEntityHutWarehouse extends TileEntityHutWorker
{
    public TileEntityHutWarehouse()
    {
        setMaxInhabitants(1);
    }

    @Override
    public String getName()
    {
        return "Warehouse";
    }

    @Override
    public String getJobName()
    {
        return "Deliveryman";
    }

    @Override
    public EntityCitizen createWorker()
    {
        return new EntityCitizen(worldObj); //TODO Implement Later
    }
}