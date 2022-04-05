package com.gabriel.helpdesk.services;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.gabriel.helpdesk.domain.Pessoa;
import com.gabriel.helpdesk.domain.Tecnico;
import com.gabriel.helpdesk.domain.dtos.TecnicoDTO;
import com.gabriel.helpdesk.repositories.PessoaRepository;
import com.gabriel.helpdesk.repositories.TecnicoRepository;
import com.gabriel.helpdesk.services.exceptions.DataIntegrityViolationException;
import com.gabriel.helpdesk.services.exceptions.ObjectNotFoundException;

@Service
public class TecnicoService {

	@Autowired
	private TecnicoRepository repository;
	
	@Autowired
	private PessoaRepository pessoaRepository;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	public Tecnico findById(Integer id) {
		Optional<Tecnico> obj = repository.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado! Id: " + id));
	}

	public List<Tecnico> findAll() {		
		return repository.findAll();
	}

	public Tecnico create(TecnicoDTO objDTO) {
		objDTO.setId(null);
		objDTO.setSenha(encoder.encode(objDTO.getSenha()));
		validaPorCPFEmail(objDTO);
		Tecnico newObj = new Tecnico(objDTO);
		return repository.save(newObj);
	}
	
	public Tecnico update(Integer id, @Valid TecnicoDTO objDTO) {
		objDTO.setId(id);
		Tecnico oldObj = findById(id);
		validaPorCPFEmail(objDTO);
		oldObj = new Tecnico(objDTO);
		return repository.save(oldObj);
	} 
	

	public void delete(Integer id) {
		Tecnico obj = findById(id);
		if(obj.getChamados().size() > 0 ) {
			throw new DataIntegrityViolationException("Técino possui ordens de serviços e não pode ser deletado!");	
		}
		repository.deleteById(id);
	}

	private void validaPorCPFEmail(TecnicoDTO objDTO) {
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
