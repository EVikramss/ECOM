import Button from 'react-bootstrap/Button';
import Form from 'react-bootstrap/Form';
import Row from 'react-bootstrap/Row';
import 'bootstrap/dist/css/bootstrap.min.css';
import './OrderSearch.css';
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import config from '../common/config.json';

function OrderSearch(props) {
    const [formData, setFormData] = useState({ orderNo: '' });
    const [errorVal, setErrorVal] = useState({ error: '' });
    const navigate = useNavigate();

    const submit = async (e) => {
        e.preventDefault();
        const orderNo = formData.orderNo;
        setError("");

        try {
            const token = props.token;
            axios.get(config.apiURL + "/getOrder?orderNo=" + orderNo, {
                headers: {
                    Authorization: token
                }
            }).then(result => {
                var orderData = result.data.getOrder;
                if (orderData == null) {
                    setError("Order not found");
                } else {
                    navigate('/orderDetail', { state: { data: orderData } });
                }
            })
                .catch(error => {
                    setError(error);
                });
        } catch (error) {
            setError(error);
        }
    }

    const setError = (error) => {
        setErrorVal({
            ...errorVal,
            error
        });
    }

    const onChangeField = (e) => {
        const { name, value } = e.target;
        setFormData({
            ...formData,
            [name]: value
        });
    }

    return (
        <div>
            <div className="orderSearch">
                <Form onSubmit={submit}>
                    <Row className="mb-3">
                        <Form.Group controlId="formGroupOrderNo">
                            <Form.Control required type="text" placeholder="Enter Order No" name="orderNo" onChange={onChangeField} />
                        </Form.Group>
                    </Row>

                    <Button variant="primary" type="submit">Submit</Button>
                </Form>

                <p></p>
                <p>{errorVal.error}</p>
            </div>
        </div>
    )
}

export default OrderSearch