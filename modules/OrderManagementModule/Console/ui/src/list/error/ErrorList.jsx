import { useParams, useLocation } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import '../../common/Common.css';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import config from '../../common/config.json';
import React, { useState, useEffect, useRef } from 'react';

function errorList(props) {
	// create state for errorData, pageNumber & errors
	const [errorListData, setErrorListData] = useState([]);
	const [pageNumber, setPageNumber] = useState(0);
	const [errorVal, setErrorVal] = useState({strVal : ''});
	let errorListSize = errorListData.length;
	const isFirstRun = useRef(true);
	
	// get state passed from search
    const { state } = useLocation();
	
	// from state extract formData (used to search), pageSize
	const openSingleError = state.openSingleError;
	const formData = state.formData;
	const pageSize = 30;
	
	// navigation hook
	const navigate = useNavigate();
	
	// fetch selected error
	const getError = async (errorKey) => {
        try {
            const token = props.token;
            axios.get(config.apiURL + "/getError?errorKey=" + errorKey, {
                headers: {
                    Authorization: token
                }
            }).then(result => {
                let errorData = result.data;
                if (errorData == null) {
                    setErrorMsg("Error key does not exist");
                } else {
					navigate('/errorDetail', { state: { data: errorData} });
                }
            }).catch(error => {
                setGeneralError(error);
            });
        } catch (error) {
            setGeneralError(error);
        }
    }
	
	// paginate over search results
	useEffect(() => {
		try {
            const token = props.token;
            axios.post(config.apiURL + "/searchError?pageSize=" + pageSize + "&pageNumber=" + pageNumber, formData, {
                headers: {
                    Authorization: token
                }
            }).then(result => {
                var errorDataArr = result.data;
                setErrorListData(errorDataArr);
				
				if (isFirstRun.current) {
					isFirstRun.current = false;
					checkAndNavigateToDetailPage(errorDataArr);	
				}
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
	
	// convert date to display format
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

	// if only 1 detail which user is looking for and if found, directly open the details page
    let checkAndNavigateToDetailPage = (dataArr) => {
		if(dataArr.length == 1 && openSingleError) {
			getError(dataArr[0].errorKey);
		}
	}

    return (
        <div className="headerSpacing">
			<button className="btn btn-outline-primary" onClick={() => navigate(-1)}>←</button>
			<div className="lineSpacing"></div>
		
			<table className="table table-bordered mb-0 rounded">
				<thead className="table-light">
					<tr>
						<th>Error Key</th>
					</tr>
				</thead>
				<tbody>
					{errorListData.map(errorKey => (
						<tr key={errorKey}>
							<td>
								<a onClick={(e) => {
									e.preventDefault();
									getError(errorKey);
									}
								} className="text-primary text-decoration-underline" style={{cursor: 'pointer'}}>
									{errorKey}
								</a>
							</td>
						</tr>
					))}
				</tbody>
			</table>
			
			<div className="position-fixed bottom-0 end-0 p-3 d-flex gap-2">
				<button className="btn btn-outline-primary" onClick={previousPage} disabled={pageNumber < 1}>←</button>
				<button className="btn btn-outline-primary" onClick={nextPage} disabled={errorListSize == 0 || errorListSize < pageSize}>→</button>
			</div>
			
			<p></p>
			<p>{errorVal.strVal}</p>
        </div>
    )
}

export default errorList