package com.minecolonies.colony;

import com.minecolonies.MineColonies;
import com.minecolonies.colony.buildings.Building;
import com.minecolonies.colony.buildings.BuildingHome;
import com.minecolonies.colony.buildings.BuildingWorker;
import com.minecolonies.configuration.Configurations;
import com.minecolonies.entity.EntityCitizen;
import com.minecolonies.util.ChunkCoordUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChunkCoordinates;

import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.UUID;

/**
 * Extra data for Citizens
 *
 */
public class CitizenData
{
    //  Attributes
    private final UUID id;
    private String     name;
    private boolean    isFemale;
    private int        textureId;

    private Colony         colony;
    private BuildingHome   homeBuilding;
    private BuildingWorker workBuilding;

    private boolean isDirty;

    //  Citizen
    public WeakReference<EntityCitizen> entity;

    //  Placeholder skills
    private int level;
    public  int strength, stamina, wisdom, intelligence, charisma;

    private static final String TAG_ID      = "id";
    private static final String TAG_NAME    = "name";
    private static final String TAG_FEMALE  = "female";
    private static final String TAG_TEXTURE = "texture";
    private static final String TAG_LEVEL   = "level";

    private static final String TAG_ENTITY_ID = "entity";
    private static final String TAG_HOME_BUILDING = "homeBuilding";
    private static final String TAG_WORK_BUILDING = "workBuilding";

    private static final String TAG_SKILLS         = "skills";
    private static final String TAG_SKILL_STRENGTH = "strength";
    private static final String TAG_SKILL_STAMINA  = "stamina";
    private static final String TAG_SKILL_WISDOM   = "wisdom";
    private static final String TAG_SKILL_INTELLIGENCE = "intelligence";
    private static final String TAG_SKILL_CHARISMA = "charisma";

    private CitizenData(UUID id, Colony colony)
    {
        this.id = id;
        this.colony = colony;
    }

    private CitizenData(EntityCitizen entity, Colony colony)
    {
        this(entity.getUniqueID(), colony);

        Random rand = entity.getRNG();

        this.entity = new WeakReference<EntityCitizen>(entity);

        isFemale = rand.nextBoolean();   //  Gender before name
        name = generateName(rand);
        textureId = entity.getTextureID();

        strength = rand.nextInt(10) + 1;
        stamina = rand.nextInt(10) + 1;
        wisdom = rand.nextInt(10) + 1;
        intelligence = rand.nextInt(10) + 1;
        charisma = rand.nextInt(10) + 1;

        markDirty();
    }

    public static CitizenData createFromNBT(NBTTagCompound compound, Colony colony)
    {
        UUID id = UUID.fromString(compound.getString(TAG_ID));
        CitizenData citizen = new CitizenData(id, colony);
        citizen.readFromNBT(compound);
        return citizen;
    }

    public static CitizenData createFromEntity(EntityCitizen entity, Colony colony)
    {
        return new CitizenData(entity, colony);
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public boolean isFemale() { return isFemale; }
    public int getTextureId() { return textureId; }
    public int getLevel() { return level; }

    public boolean isDirty() { return isDirty; }
    public void markDirty()
    {
        isDirty = true;
        colony.markCitizensDirty();
    }
    public void clearDirty() { isDirty = false; }

    public BuildingHome getHomeBuilding() { return homeBuilding; }
    public void setHomeBuilding(BuildingHome building)
    {
        if (homeBuilding != null && building != null && homeBuilding != building)
        {
            throw new IllegalStateException("CitizenData.setHomeBuilding() - already assigned a home building when setting a new home building");
        }
        else if (homeBuilding != building)
        {
            homeBuilding = building;
            markDirty();
        }
    }

    public BuildingWorker getWorkBuilding() { return workBuilding; }
    public void setWorkBuilding(BuildingWorker building)
    {
        if (workBuilding != null && building != null && workBuilding != building)
        {
            throw new IllegalStateException("CitizenData.setWorkBuilding() - already assigned a work building when setting a new work building");
        }
        else if (workBuilding != building)
        {
            workBuilding = building;
            markDirty();
        }
    }

    /**
     * When a building is destroyed, inform the citizen so it can do any cleanup of associations that the building's
     * own Building.onDestroyed did not do.
     *
     * @param building
     */
    public void onRemoveBuilding(Building building)
    {
        if (getHomeBuilding() == building)
        {
            homeBuilding = null;
        }

        if (getWorkBuilding() == building)
        {
            workBuilding = null;
        }
    }

    public EntityCitizen getCitizenEntity() { return (entity != null) ? entity.get() : null; }
    public void setCitizenEntity(EntityCitizen citizen)
    {
        if (!citizen.getUniqueID().equals(id))
        {
            throw new IllegalArgumentException(String.format("Mismatch citizen '%s' registered to CitizenData for '%s'", citizen.getUniqueID().toString(), id.toString()));
        }

        entity = new WeakReference<EntityCitizen>(citizen);
        markDirty();
    }
    public void clearCitizenEntity()
    {
        entity = null;
    }

    public void writeToNBT(NBTTagCompound compound)
    {
        compound.setString(TAG_ID, id.toString());
        compound.setString(TAG_NAME, name);
        compound.setBoolean(TAG_FEMALE, isFemale);
        compound.setInteger(TAG_TEXTURE, textureId);

        //  Attributes
        compound.setInteger(TAG_LEVEL, level);

        NBTTagCompound nbtTagSkillsCompound = new NBTTagCompound();
        nbtTagSkillsCompound.setInteger(TAG_SKILL_STRENGTH, strength);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_STAMINA, stamina);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_WISDOM, wisdom);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_INTELLIGENCE, intelligence);
        nbtTagSkillsCompound.setInteger(TAG_SKILL_CHARISMA, charisma);
        compound.setTag(TAG_SKILLS, nbtTagSkillsCompound);
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        name = compound.getString(TAG_NAME);
        isFemale = compound.getBoolean(TAG_FEMALE);
        textureId = compound.getInteger(TAG_TEXTURE);

        //  Attributes
        level = compound.getInteger(TAG_LEVEL);

        NBTTagCompound nbtTagSkillsCompound = compound.getCompoundTag("skills");
        strength = nbtTagSkillsCompound.getInteger("strength");
        stamina = nbtTagSkillsCompound.getInteger("stamina");
        wisdom = nbtTagSkillsCompound.getInteger("wisdom");
        intelligence = nbtTagSkillsCompound.getInteger("intelligence");
        charisma = nbtTagSkillsCompound.getInteger("charisma");
    }

    private String generateName(Random rand)
    {
        String firstName;
        if(!isFemale)
        {
            firstName = getRandomElement(rand, Configurations.maleFirstNames);
        }
        else
        {
            firstName = getRandomElement(rand, Configurations.femaleFirstNames);
        }
        return String.format("%s %s. %s", firstName, getRandomLetter(rand), getRandomElement(rand, Configurations.lastNames));
    }

    private String getRandomElement(Random rand, String[] array)
    {
        return array[rand.nextInt(array.length)];
    }

    private char getRandomLetter(Random rand)
    {
        return (char) (rand.nextInt(26) + 'A');
    }


    /**
     * The Building View is the client-side representation of a Building.
     * Views contain the Building's data that is relevant to a Client, in a more client-friendly form
     * Mutable operations on a View result in a message to the server to perform the operation
     */
    public static class View
    {
        private final UUID id;
        private int        entityId;
        private String     name;
        private boolean    isFemale;

        //  Placeholder skills
        private int level;
        public  int strength, stamina, wisdom, intelligence, charisma;

        private ChunkCoordinates homeBuilding;
        private ChunkCoordinates workBuilding;

        protected View(UUID id)
        {
            this.id = id;
        }

        public UUID getID(){ return id; }

        public int getEntityId(){ return entityId; }

        public String getName(){ return name; }

        public boolean isFemale(){ return isFemale; }

        public int getLevel(){ return level; }

        public ChunkCoordinates getHomeBuilding(){ return homeBuilding; }

        public ChunkCoordinates getWorkBuilding(){ return workBuilding; }

        public void parseNetworkData(NBTTagCompound compound)
        {
            //  TODO - Use a PacketBuffer
            name = compound.getString(TAG_NAME);
            isFemale = compound.getBoolean(TAG_FEMALE);
            entityId = compound.hasKey(TAG_ENTITY_ID) ? compound.getInteger(TAG_ENTITY_ID) : -1;

            homeBuilding = compound.hasKey(TAG_HOME_BUILDING) ? ChunkCoordUtils.readFromNBT(compound, TAG_HOME_BUILDING) : null;
            workBuilding = compound.hasKey(TAG_WORK_BUILDING) ? ChunkCoordUtils.readFromNBT(compound, TAG_WORK_BUILDING) : null;

            //  Attributes
            level = compound.getInteger(TAG_LEVEL);

            NBTTagCompound skillsCompound = compound.getCompoundTag(TAG_SKILLS);
            strength = skillsCompound.getInteger(TAG_SKILL_STRENGTH);
            stamina = skillsCompound.getInteger(TAG_SKILL_STAMINA);
            wisdom = skillsCompound.getInteger(TAG_SKILL_WISDOM);
            intelligence = skillsCompound.getInteger(TAG_SKILL_INTELLIGENCE);
            charisma = skillsCompound.getInteger(TAG_SKILL_CHARISMA);
        }
    }

    public void createViewNetworkData(NBTTagCompound compound)
    {
        //  TODO - Use a PacketBuffer
        compound.setString(TAG_NAME, name);
        compound.setBoolean(TAG_FEMALE, isFemale);

        EntityCitizen entity = getCitizenEntity();
        if (entity != null)
        {
            compound.setInteger(TAG_ENTITY_ID, entity.getEntityId());
        }

        if (homeBuilding != null)
        {
            ChunkCoordUtils.writeToNBT(compound, TAG_HOME_BUILDING, homeBuilding.getID());
        }
        if (workBuilding != null)
        {
            ChunkCoordUtils.writeToNBT(compound, TAG_WORK_BUILDING, workBuilding.getID());
        }

        //  Attributes
        compound.setInteger(TAG_LEVEL, level);

        NBTTagCompound skillsCompound = new NBTTagCompound();
        skillsCompound.setInteger(TAG_SKILL_STRENGTH, strength);
        skillsCompound.setInteger(TAG_SKILL_STAMINA, stamina);
        skillsCompound.setInteger(TAG_SKILL_WISDOM, wisdom);
        skillsCompound.setInteger(TAG_SKILL_INTELLIGENCE, intelligence);
        skillsCompound.setInteger(TAG_SKILL_CHARISMA, charisma);
        compound.setTag(TAG_SKILLS, skillsCompound);
    }

    /**
     * Create a CitizenData View given it's saved NBTTagCompound
     * TODO - Use a PacketBuffer
     *
     * @param id       The citizen's id
     * @param compound The network data
     * @return
     */
    public static View createCitizenDataView(UUID id, NBTTagCompound compound)
    {
        View view = new View(id);

        try
        {
            view.parseNetworkData(compound);
        }
        catch (Exception ex)
        {
            MineColonies.logger.error(String.format("A CitizenData.View for %s has thrown an exception during loading, its state cannot be restored. Report this to the mod author", view.getID().toString()), ex);
            view = null;
        }

        return view;
    }
}