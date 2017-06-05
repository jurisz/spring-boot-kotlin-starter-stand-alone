package com.twino.ls.base.model;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Predicate;
import com.twino.ls.base.util.DateTimeUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OptimisticLock;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.time.LocalDateTime;

import static com.google.common.base.Preconditions.checkNotNull;

@MappedSuperclass
@BatchSize(size = 10)
public abstract class BaseEntity {

	@Id
	@GeneratedValue(generator = "seq_gen", strategy = GenerationType.SEQUENCE)
	@Column(name = "id")
	private Long id;

	@Version
	@Column(name = "entity_version", nullable = false)
	private Long entityVersion;

	@OptimisticLock(excluded = true)
	@Column(name = "entity_created", nullable = false)
	private LocalDateTime created = DateTimeUtils.now();

	@OptimisticLock(excluded = true)
	@Column(name = "entity_updated", nullable = false)
	private LocalDateTime updated = DateTimeUtils.now();

	public Long getId() {
		return id;
	}

	public Long getEntityVersion() {
		return entityVersion;
	}

	public LocalDateTime getCreated() {
		return created;
	}

	public LocalDateTime getUpdated() {
		return updated;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this).add("id", getId()).toString();
	}

	public void setUpdated(LocalDateTime updated) {
		this.updated = updated;
	}

	@PreUpdate
	public void preUpdate() {
		this.updated = DateTimeUtils.now();
	}

	@SuppressWarnings("unchecked")
	public <T, F> T apply(Function<F, T> function) {
		return function.apply((F) this);
	}

	@SuppressWarnings("unchecked")
	public <T extends BaseEntity> boolean satisfies(Predicate<T> specification) {
		return specification.apply((T) this);
	}

	public <T extends BaseEntity> boolean isSameEntity(T otherEntity) {
		if (otherEntity == null) {
			return false;
		}
		if (this == otherEntity) {
			return true;
		}
		Class<?> otherClass = unwrapHibernateProxyClass(otherEntity);
		if (!this.getClass().equals(otherClass)) {
			return false;
		}
		checkNotNull(id, "Entity must be persisted %s", this);
		return id.equals(otherEntity.getId());
	}

	private Class<?> unwrapHibernateProxyClass(Object entity) {
		return entity instanceof HibernateProxy ? ((HibernateProxy) entity).getHibernateLazyInitializer().getImplementation().getClass() : entity.getClass();
	}

}


