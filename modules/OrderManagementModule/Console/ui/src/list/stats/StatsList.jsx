import { useParams, useLocation } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../../common/Common.css';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import config from '../../common/config.json';
import React, { useState, useEffect, useRef } from 'react';

function statsList(props) {
	// create state for data, pageNumber & errors
	const [statsListData, setStatsListData] = useState([]);
	const [pageNumber, setPageNumber] = useState(0);
	const [errorVal, setErrorVal] = useState({strVal : ''});
	let statsListSize = statsListData.length;
	
	// get state passed from search
    const { state } = useLocation();
	
	// from state extract formData (used to search), pageSize
	const formData = state.formData;
	const pageSize = 30;
	
	// navigation hook
	const navigate = useNavigate();
	
	// paginate over search results
	useEffect(() => {
		try {
            const token = props.token;
            axios.post(config.apiURL + "/searchStats?pageSize=" + pageSize + "&pageNumber=" + pageNumber, formData, {
                headers: {
                    Authorization: token
                }
            }).then(result => {
                var statsDataArr = result.data;
                setStatsListData(statsDataArr);
            }).catch(error => {
                setGeneralError(error);
            });
        } catch (error) {
            setGeneralError(error);
        }
	}, [pageNumber]);
	
	const previousPage = () => {
		if(pageNumber >= 1) {
			setPageNumber(pageNumber - 1);	
		}		
	}
	
	const nextPage = () => {
		setPageNumber(pageNumber + 1);
	}
	
	// convert to display format
	const convertDateField = (inputDate) => {
		const date = new Date(inputDate);		
		const formatted = date.toLocaleString(undefined, {
		  year: 'numeric',
		  month: '2-digit',
		  day: '2-digit',
		  hour: '2-digit',
		  minute: '2-digit',
		  second: '2-digit',
		  hour12: false
		});

		const formattedDate = formatted.replace(/\//g, '-').replace(',', '');
		
		return formattedDate;
	};
	
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

    return (
        <div className="headerSpacing">
			<button className="btn btn-outline-primary" onClick={() => navigate(-1)}>←</button>
			<div className="lineSpacing"></div>
		
			<table className="table table-bordered mb-0 rounded">
				<thead className="table-light">
					<tr>
						<th>Service name</th>
						<th>Function name</th>
						<th>From Time</th>
						<th>To Time</th>
						<th>Count</th>
						<th>Avg.</th>
						<th>Max</th>
						<th>Min</th>
					</tr>
				</thead>
				<tbody>
					{statsListData.map(stat => (
						<tr key={stat.statsKey}>
							<td>{stat.service}</td>
							<td>{stat.functionName}</td>
							<td>{convertDateField(stat.fromTime)}</td>
							<td>{convertDateField(stat.toTime)}</td>
							<td>{stat.count}</td>
							<td>{stat.average}</td>
							<td>{stat.max}</td>
							<td>{stat.min}</td>
						</tr>
					))}
				</tbody>
			</table>
			
			<div className="position-fixed bottom-0 end-0 p-3 d-flex gap-2">
				<button className="btn btn-outline-primary" onClick={previousPage} disabled={pageNumber < 1}>←</button>
				<button className="btn btn-outline-primary" onClick={nextPage} disabled={statsListSize == 0 || statsListSize < pageSize}>→</button>
			</div>
			
			<p></p>
			<p>{errorVal.strVal}</p>
        </div>
    )
}

export default statsList