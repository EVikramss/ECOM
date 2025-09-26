import Container from 'react-bootstrap/Container';
import Nav from 'react-bootstrap/Nav';
import Navbar from 'react-bootstrap/Navbar';
import NavDropdown from 'react-bootstrap/NavDropdown';
import { useNavigate } from 'react-router-dom';
import 'bootstrap/dist/css/bootstrap.min.css';
import './Header.css'

function Header() {
    const navigate = useNavigate();

    return (
        <Navbar expand="lg" fixed="top" className="w-100 bg-body-tertiary headerBar">
            <Container fluid>
                <Navbar.Brand href="#" onClick={() => navigate('/')}>Console</Navbar.Brand>
                <Navbar.Toggle aria-controls="basic-navbar-nav" />
                <Navbar.Collapse id="basic-navbar-nav">
                    <Nav className="me-auto">
						<Nav.Link onClick={() => navigate('/orderSearch')}>Orders</Nav.Link>
						<Nav.Link onClick={() => navigate('/errorSearch')}>Errors</Nav.Link>
						<Nav.Link onClick={() => navigate('/statsSearch')}>Stats</Nav.Link>
                    </Nav>
                </Navbar.Collapse>
            </Container>
        </Navbar>
    )
}

export default Header