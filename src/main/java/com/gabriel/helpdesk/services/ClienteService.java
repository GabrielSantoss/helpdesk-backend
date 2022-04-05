package com.gabriel.helpdesk.services;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.gabriel.helpdesk.domain.Pessoa;
import com.gabriel.helpdesk.domain.Cliente;
import com.gabriel.helpdesk.domain.dtos.ClienteDTO;
import com.gabriel.helpdesk.repositories.PessoaRepository;
import com.gabriel.helpdesk.repositories.ClienteRepository;
import com.gabriel.helpdesk.services.exceptions.DataIntegrityViolationException;
import com.gabriel.helpdesk.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository repository;
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	public Cliente findById(Integer id) {
		Optional<Cliente> obj = repository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado! Id: " + id));
	}

	public List<Cliente> findAll() {		
		return repository.findAll();
	}

	public Cliente create(ClienteDTO objDTO) {
		objDTO.setId(null);
		objDTO.setSenha(encoder.encode(objDTO.getSenha()));
		validaPorCPFEmail(objDTO);
		Cliente newObj = new Cliente(objDTO);
		return repository.save(newObj);
	}
	
	public Cliente update(Integer id, @Valid ClienteDTO objDTO) {
		objDTO.setId(id);
		Cliente oldObj = findById(id);
		validaPorCPFEmail(objDTO);
		oldObj = new Cliente(objDTO);
		return repository.save(oldObj);
	} 
	

	public void delete(Integer id) {
		Cliente obj = findById(id);
		if(obj.getChamados().size() > 0 ) {
			throw new DataIntegrityViolationException("Cliente possui ordens de serviços e não pode ser deletado!");	
		}
		repository.deleteById(id);
	}

	private void validaPorCPFEmail(ClienteDTO objDTO) {
		Optional<Pessoa> obj = pessoaRepository.findByCpf(objDTO.getCpf());
	    if (obj.isPresent() && obj.get().getId() != objDTO.getId()) {
	    	throw new DataIntegrityViolationException("CPF já cadastrado no sistema");
	    }
			
	    obj = pessoaRepository.findByEmail(objDTO.getEmail());
	    if (obj.isPresent() && obj.get().getId() != objDTO.getId()) {
	    	throw new DataIntegrityViolationException("Email já cadastrado no sistema");
	    }
	}

}
