import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import Row from 'react-bootstrap/Row';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../../common/Common.css';
import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import config from '../../common/config.json';

function statsSearch(props) {
    // store form data as state
    const [formData, setFormData] = useState({ service: '', fromDate: '', toDate: '', sortOrder: 0, sortByField : 0 });
    const [errorVal, setErrorVal] = useState({strVal : ''});
	const [serviceNameList, setServiceNameList] = useState([]);
    const navigate = useNavigate();

    const searchStats = async (e) => {
        e.preventDefault();
		navigate('/statsList', { state: { formData : formData} });
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
	
	useEffect(() => {
		try {
            const token = props.token;
            axios.get(config.apiURL + "/getStatServiceNames", {
                headers: {
                    Authorization: token
                }
            }).then(result => {
                var serviceNameList = result.data;
                setServiceNameList(serviceNameList);
            }).catch(error => {
                setGeneralError(error);
            });
        } catch (error) {
            setGeneralError(error);
        }		
	}, []);

    return (
        <div className="headerSpacing">
            <Form className="container mt-4" onSubmit={searchStats}>
                <div className="row align-items-end">
                    <div className="col-md-3">
                        <label className="form-label">Service</label>
						<select id="dropdown" className="form-select" name="service" onChange={onChangeField}>
							<option value=""></option>
								{serviceNameList.map((service, index) => (
									<option key={index} value={service}>{service}</option>
								))}
						</select>
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

export default statsSearch