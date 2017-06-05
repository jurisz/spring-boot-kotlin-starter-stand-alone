package com.twino.ls.base.model;

import com.twino.ls.api.operationlog.OperationLogPropertyBean;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;

@SequenceGenerator(name = "seq_gen", sequenceName = "operation_log_properties_seq", allocationSize = 1)
@Audited
@Entity
@Table(name = "operation_log_properties", indexes =
		{
				@Index(name = "idx_operation_log_properties_operation_log_id", columnList = "operation_log_id")
		})
@BatchSize(size = 10)
public class OperationLogProperty extends BaseEntity {

	private static final int MAX_VALUE_LENGTH = 1000;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "operation_log_id")
	private OperationLog operationLog;

	@Column(name = "name", nullable = false, length = 100)
	private String name;

	@Column(name = "value", nullable = false, length = MAX_VALUE_LENGTH)
	private String value;

	OperationLogProperty() {
	}

	public OperationLogProperty(OperationLog operationLog, String name, String value) {
		this.operationLog = operationLog;
		this.name = name;
		setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = StringUtils.abbreviate(value, MAX_VALUE_LENGTH);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean hasName(String name) {
		return this.name.equals(name);
	}

	public OperationLogPropertyBean toBean() {
		OperationLogPropertyBean bean = new OperationLogPropertyBean();
		BeanUtils.copyProperties(this, bean);
		return bean;
	}
}
