package com.twino.ls.base.model;

import com.google.common.collect.Iterables;
import com.twino.ls.api.operationlog.OperationLogBean;
import com.twino.ls.api.operationlog.OperationLogShortBean;
import com.twino.ls.base.util.HumanStringJoiner;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.BatchSize;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Iterables.transform;
import static com.twino.ls.base.util.DateTimeUtils.now;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@SequenceGenerator(name = "seq_gen", sequenceName = "operation_log_seq", allocationSize = 1)
@Audited
@Entity
@Table(name = "operation_log")
@BatchSize(size = 10)
public class OperationLog extends BaseEntity {

	@Column(name = "type", length = 50)
	private String type;

	@Column(name = "title")
	private String title;

	@Column(name = "start_time")
	private LocalDateTime startDate = now();

	@Column(name = "end_time")
	private LocalDateTime endDate = now();

	@OneToMany(mappedBy = "operationLog", cascade = CascadeType.ALL)
	@OrderBy("id asc")
	@NotAudited
	private List<OperationLogProperty> properties = new ArrayList<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDateTime getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDateTime startDate) {
		this.startDate = startDate;
	}

	public LocalDateTime getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDateTime endDate) {
		this.endDate = endDate;
	}

	public List<OperationLogProperty> getProperties() {
		return copyOf(properties);
	}

	public String printProperties() {
		return HumanStringJoiner.on(", ").join(transform(properties, (prop) -> prop.getName() + "=" + prop.getValue()));
	}

	public String getFormattedDuration() {
		if (startDate == null || endDate == null) {
			return null;
		}

		Duration duration = Duration.between(startDate, endDate);
		return duration.toString();
	}

	public void addProperty(String name, String value) {
		if (isNotEmpty(name) && isNotEmpty(value)) {
			properties.add(new OperationLogProperty(this, name, value));
		}
	}

	public void addOrReplaceProperty(String name, String value) {
		if (isNotEmpty(name) && isNotEmpty(value)) {
			OperationLogProperty property = Iterables.find(properties, (p) -> p.hasName(name), null);
			if (property != null) {
				property.setValue(value);
			} else {
				addProperty(name, value);
			}
		}
	}

	public void incrementCountingProperty(String name) {
		String value = getPropertyValue(name);
		if (StringUtils.isEmpty(value)) {
			addOrReplaceProperty(name, "1");
			return;
		}
		try {
			Integer counter = Integer.valueOf(value);
			counter++;
			addOrReplaceProperty(name, "" + counter);
		} catch (NumberFormatException e) {
			addOrReplaceProperty(name, "1");
		}
	}

	public String getPropertyValue(String name) {
		OperationLogProperty property = Iterables.find(properties, (p) -> p.hasName(name), null);
		return property != null ? property.getValue() : null;
	}

	public OperationLogBean toBean() {
		OperationLogBean bean = new OperationLogBean();
		BeanUtils.copyProperties(this, bean, "properties");
		bean.setFormattedDuration(getFormattedDuration());
		properties.forEach((input) -> bean.getProperties().add(input.toBean()));
		return bean;
	}

	public OperationLogShortBean toShortBean() {
		OperationLogShortBean bean = new OperationLogShortBean();
		BeanUtils.copyProperties(this, bean);
		bean.setFormattedDuration(getFormattedDuration());
		return bean;
	}

}
