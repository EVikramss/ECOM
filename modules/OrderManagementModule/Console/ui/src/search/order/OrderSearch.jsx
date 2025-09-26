import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import Row from 'react-bootstrap/Row';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../../common/Common.css';
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import config from '../../common/config.json';

function orderSearch(props) {
    // store form data as state
    const [formData, setFormData] = useState({ orderNo: '', entity: '', matchType: 'Exact', fromDate: '', toDate: '', sortOrder: 0, sortByField : 0 });
    const [errorVal, setErrorVal] = useState({strVal : ''});
    const navigate = useNavigate();

    const searchOrder = async (e) => {
        e.preventDefault();
		let openOrderIntent = isUserIntentToOpenOrder(formData);
		navigate('/orderList', { state: { openSingleOrder : openOrderIntent, formData : formData} });
    }
	
	const isUserIntentToOpenOrder = (data) => {
		// if order No is given with match type Exact, assume user intent is to directly open the order
		if(data.matchType === 'Exact' && isValidString(data.orderNo)) {
			return true;
		} else {
			return false;
		}
	}	
	
	const isValidString = (str) => {
		return typeof str === 'string' && str.trim().length > 0;
	}

    const setGeneralError = (error) => {
		let message = 'Error'
		if(error.message)
			message = error.message
		
        setErrorVal({
            ...errorVal,
            strVal : message
        });
    }
	
	const setErrorMsg = (errorMsg) => {		
        setErrorVal({
            ...errorVal,
            strVal : errorMsg
        });
    }

    const onChangeField = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
    }
	
	const onChangeFieldBoolean = (e) => {
        const { name, value } = e.target;
		let booleanValue = false;
		booleanValue = value === "true";
        setFormData({
            ...formData,
            [name]: booleanValue
        });
    }
	
	const onChangeFieldInt = (e) => {
        const { name, value } = e.target;
		let intValue = parseInt(value);
        setFormData({
            ...formData,
            [name]: intValue
        });
    }

    return (
        <div className="headerSpacing">
            <Form className="container mt-4" onSubmit={searchOrder}>
                <div className="row align-items-end">
                    <div className="col-md-3">
                        <label className="form-label">Order No</label>
                        <input type="text" placeholder="Enter Order No" name="orderNo" className="form-control" onChange={onChangeField} />
                    </div>

                    <div className="col-md-2">
                        <label className="form-label">Match Type</label>
                        <select className="form-select" name="matchType" value={formData.matchType} onChange={onChangeField}>
                            <option value="Exact">Exact</option>
                            <option value="Like">Like</option>
                        </select>
                    </div>
                </div>

                <div className="lineSpacing"></div>

                <div className="row align-items-end">
                    <div className="col-md-3">
                        <label className="form-label">Entity</label>
                        <input type="text" placeholder="Enter Order Entity" className="form-control" name="entity" onChange={onChangeField}
                        />
                    </div>
                </div>

                <div className="lineSpacing"></div>

                <div className="row align-items-end">
                    <div className="col-md-3">
                        <label className="form-label">From Date</label>
                        <input type="date" className="form-control" name="fromDate" onChange={onChangeField} />
                    </div>
                </div>

                <div className="lineSpacing"></div>

                <div className="row align-items-end">
                    <div className="col-md-3">
                        <label className="form-label">To Date</label>
                        <input type="date" className="form-control" name="toDate" onChange={onChangeField} />
                    </div>
                </div>
				
				<div className="lineSpacing"></div>
				
				<div className="row align-items-end">
					<label className="form-label">Sort</label>
					
					<div className="d-flex gap-4">
						<div className="d-flex gap-0 rounded border px-3 py-2">
							<div className="form-check form-check-inline">
								<input type="radio" checked={formData.sortOrder === 0} className="form-check-input" id="Asc" name="sortOrder" value="0" onChange={onChangeFieldInt} />
								<label className="form-check-label" htmlFor="Asc">Asc</label>
							</div>
							
							<div className="form-check form-check-inline">
								<input type="radio" checked={formData.sortOrder === 1} className="form-check-input" id="Dsc" name="sortOrder" value="1" onChange={onChangeFieldInt} />
								<label className="form-check-label" htmlFor="Dsc">Dsc</label>
							</div>
						</div>
						
						<label className="form-label py-2">On</label>
						
						<div className="d-flex gap-0 rounded border px-3 py-2">
							<div className="form-check form-check-inline">
								<input type="radio" checked={formData.sortByField === 0} className="form-check-input" id="orderDate" name="sortByField" value="0" onChange={onChangeFieldInt} />
								<label className="form-check-label" htmlFor="Asc">Order Date</label>
							</div>
							
							<div className="form-check form-check-inline">
								<input type="radio" checked={formData.sortByField === 1} className="form-check-input" id="orderNo" name="sortByField" value="1" onChange={onChangeFieldInt} />
								<label className="form-check-label" htmlFor="Dsc">Order No</label>
							</div>
							
							<div className="form-check form-check-inline">
								<input type="radio" checked={formData.sortByField === 2} className="form-check-input" id="entity" name="sortByField" value="2" onChange={onChangeFieldInt} />
								<label className="form-check-label" htmlFor="Dsc">Entity</label>
							</div>
						</div>
					</div>
				</div>

                <div className="lineSpacing"></div>

                <div className="row align-items-end">
                    <div className="col-md-3 text-end">
                        <button type="submit" className="btn btn-primary w-100">Search</button>
                    </div>
                </div>
            </Form>
			
			<p></p>
			<p>{errorVal.strVal}</p>
        </div>
    )
}

export default orderSearch