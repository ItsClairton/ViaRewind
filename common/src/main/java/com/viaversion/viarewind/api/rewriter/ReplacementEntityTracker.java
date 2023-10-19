package com.viaversion.viarewind.api.rewriter;

import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.model.MetaIndex1_7_6_10To1_8;
import com.viaversion.viarewind.protocol.protocol1_7_6_10to1_8.types.metadata.MetaType1_7_6_10;
import com.viaversion.viaversion.api.connection.StoredObject;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.data.entity.ClientEntityIdChangeListener;
import com.viaversion.viaversion.api.minecraft.entities.Entity1_10Types;
import com.viaversion.viaversion.api.minecraft.metadata.Metadata;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.util.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReplacementEntityTracker extends StoredObject implements ClientEntityIdChangeListener {
	private final Map<Entity1_10Types.EntityType, Pair<Entity1_10Types.EntityType, String>> ENTITY_REPLACEMENTS = new HashMap<>();

	private final Map<Integer, Entity1_10Types.EntityType> entityMap = new HashMap<>();
	private final Map<Integer, Entity1_10Types.EntityType> entityReplacementMap = new HashMap<>();

	private int playerId;

	private final ProtocolVersion version;

	public ReplacementEntityTracker(UserConnection user, final ProtocolVersion version) {
		super(user);
		this.version = version;
	}

	public void registerEntity(final Entity1_10Types.EntityType oldType, final Entity1_10Types.EntityType newType, final String name) {
		ENTITY_REPLACEMENTS.put(oldType, new Pair<>(newType, this.version.getName() + " " + name));
	}

	public void addEntity(final int entityId, final Entity1_10Types.EntityType type) {
		entityMap.put(entityId, type);
	}

	public int replaceEntity(final int entityId, final Entity1_10Types.EntityType type) {
		entityReplacementMap.put(entityId, type);

		return ENTITY_REPLACEMENTS.get(type).key().getId();
	}

	public void removeEntity(final int entityId) {
		entityMap.remove(entityId);
		entityReplacementMap.remove(entityId);
	}

	public void clear() {
		entityMap.clear();
		entityReplacementMap.clear();
	}

	public boolean isReplaced(final Entity1_10Types.EntityType type) {
		return ENTITY_REPLACEMENTS.containsKey(type);
	}

	public void updateMetadata(final int entityId, final List<Metadata> metadata) {
		final String name = ENTITY_REPLACEMENTS.get(entityMap.get(entityId)).value();

		metadata.add(new Metadata(MetaIndex1_7_6_10To1_8.ENTITY_LIVING_NAME_TAG_VISIBILITY.getNewIndex(), MetaType1_7_6_10.Byte, (byte) 1)); // TODO: Make this definable for 1.8 -> 1.9 ?
		metadata.add(new Metadata(MetaIndex1_7_6_10To1_8.ENTITY_LIVING_NAME_TAG.getNewIndex(), MetaType1_7_6_10.String, name));
	}

	@Override
	public void setClientEntityId(int entityId) {
		removeEntity(this.playerId);
		addEntity(entityId, Entity1_10Types.EntityType.ENTITY_HUMAN);

		this.playerId = entityId;
	}

	public Map<Integer, Entity1_10Types.EntityType> getEntityMap() {
		return entityMap;
	}

	public Map<Integer, Entity1_10Types.EntityType> getEntityReplacementMap() {
		return entityReplacementMap;
	}

	public int getPlayerId() {
		return playerId;
	}
}
