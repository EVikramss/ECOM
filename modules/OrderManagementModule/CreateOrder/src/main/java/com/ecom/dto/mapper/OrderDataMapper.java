package com.ecom.dto.mapper;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.ecom.entity.CustomerContact;
import com.ecom.entity.OrderAddress;
import com.ecom.entity.OrderData;
import com.ecom.entity.OrderItemData;
import com.ecom.entity.OrderStatus;

@Component
public class OrderDataMapper {

	public OrderData convertFromDTO(com.ecom.dto.OrderData inputData) {
		OrderData orderData = new OrderData();
		orderData.setOrderNo(inputData.getOrderNo());
		orderData.setOrderDate(inputData.getOrderDate());
		orderData.setEntity(inputData.getEntity());
		
		com.ecom.dto.OrderAddress inputAddress = inputData.getAddress();
		OrderAddress oa = new OrderAddress();
		oa.setAddressline1(inputAddress.getAddressline1());
		oa.setAddressline2(inputAddress.getAddressline2());
		oa.setCity(inputAddress.getCity());
		oa.setCountry(inputAddress.getCountry());
		oa.setState(inputAddress.getState());
		
		com.ecom.dto.CustomerContact inputCustomerContact = inputData.getCustomerContact();
		CustomerContact cc = new CustomerContact();
		cc.setEmail(inputCustomerContact.getEmail());
		cc.setFirstName(inputCustomerContact.getFirstName());
		cc.setLastName(inputCustomerContact.getLastName());
		cc.setPhone(inputCustomerContact.getPhone());
		cc.setSalutation(inputCustomerContact.getSalutation());
		
		Set<com.ecom.dto.OrderItemData> inputItemDataSet = inputData.getItemData();
		Set<OrderItemData> itemDataSet = new HashSet<OrderItemData>();
		
		Iterator<com.ecom.dto.OrderItemData> iter = inputItemDataSet.iterator();
		while(iter.hasNext()) {
			com.ecom.dto.OrderItemData inputItemData = iter.next();
			OrderItemData itemData = new OrderItemData();
			itemData.setLineno(inputItemData.getLineno());
			itemData.setQty(inputItemData.getQty());
			itemData.setStatus(inputItemData.getStatus());
			itemData.setSku(inputItemData.getSku());
			itemDataSet.add(itemData);
		}
		
		com.ecom.dto.OrderStatus inputOrderStatus = inputData.getOrderStatus();
		OrderStatus os = new OrderStatus();
		if(inputOrderStatus != null)
			os.setStatus(inputOrderStatus.getStatus());
		
		orderData.setAddress(oa);
		orderData.setCustomerContact(cc);
		orderData.setItemData(itemDataSet);
		orderData.setOrderStatus(os);
		
		return orderData;
	}

	public com.ecom.dto.OrderData convertToDTO(OrderData inputData) {
		com.ecom.dto.OrderData orderData = new com.ecom.dto.OrderData();
		orderData.setOrderNo(inputData.getOrderNo());
		orderData.setOrderDate(inputData.getOrderDate());
		orderData.setEntity(inputData.getEntity());
		
		OrderAddress inputAddress = inputData.getAddress();
		com.ecom.dto.OrderAddress oa = new com.ecom.dto.OrderAddress();
		oa.setAddressline1(inputAddress.getAddressline1());
		oa.setAddressline2(inputAddress.getAddressline2());
		oa.setCity(inputAddress.getCity());
		oa.setCountry(inputAddress.getCountry());
		oa.setState(inputAddress.getState());
		
		CustomerContact inputCustomerContact = inputData.getCustomerContact();
		com.ecom.dto.CustomerContact cc = new com.ecom.dto.CustomerContact();
		cc.setEmail(inputCustomerContact.getEmail());
		cc.setFirstName(inputCustomerContact.getFirstName());
		cc.setLastName(inputCustomerContact.getLastName());
		cc.setPhone(inputCustomerContact.getPhone());
		cc.setSalutation(inputCustomerContact.getSalutation());
		
		Set<OrderItemData> inputItemDataSet = inputData.getItemData();
		Set<com.ecom.dto.OrderItemData> itemDataSet = new HashSet<com.ecom.dto.OrderItemData>();
		Iterator<OrderItemData> iter = inputItemDataSet.iterator();
		while(iter.hasNext()) {
			OrderItemData inputItemData = iter.next();
			com.ecom.dto.OrderItemData itemData = new com.ecom.dto.OrderItemData();
			itemData.setLineno(inputItemData.getLineno());
			itemData.setQty(inputItemData.getQty());
			itemData.setStatus(inputItemData.getStatus());
			itemData.setSku(inputItemData.getSku());
			itemDataSet.add(itemData);
		}
		
		OrderStatus inputOrderStatus = inputData.getOrderStatus();
		com.ecom.dto.OrderStatus os = new com.ecom.dto.OrderStatus();
		os.setStatus(inputOrderStatus.getStatus());
		
		orderData.setAddress(oa);
		orderData.setCustomerContact(cc);
		orderData.setItemData(itemDataSet);
		orderData.setOrderStatus(os);
		
		return orderData;
	}
}
