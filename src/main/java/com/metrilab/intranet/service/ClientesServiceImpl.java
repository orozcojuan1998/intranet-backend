package com.metrilab.intranet.service;

import com.metrilab.intranet.dto.ClienteDTO;
import com.metrilab.intranet.modelo.*;
import com.metrilab.intranet.repository.ClienteRepository;
import com.metrilab.intranet.repository.ContactoRepository;
import com.metrilab.intranet.repository.SedeRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClientesServiceImpl implements ClientesService {

    private final ClienteRepository clienteRepository;
    private final ContactoRepository contactoRepository;
    private final SedeRepository sedeRepository;

    public ClientesServiceImpl(ClienteRepository clienteRepository, ContactoRepository contactoRepository, SedeRepository sedeRepository) {
        this.clienteRepository = clienteRepository;
        this.contactoRepository = contactoRepository;
        this.sedeRepository = sedeRepository;
    }

    public Cliente getClienteById(String id) {
        Optional<Cliente> cliente = clienteRepository.findById(Long.valueOf(id));
        return cliente.orElseGet(Cliente::new);
    }

    public Cliente createClientContact(String idClient, Contacto contact, String sedeId) {
        Optional<Cliente> cliente = clienteRepository.findById(Long.valueOf(idClient));
        Optional<Sede> sede = sedeRepository.findById(Long.valueOf(sedeId));
        if (cliente.isPresent() && sede.isPresent()) {
            contact.setCliente(cliente.get());
            contact.setSede(sede.get());
            Contacto contacto = contactoRepository.save(contact);
            cliente.get().addContact(contacto);
            clienteRepository.save(cliente.get());
            List<Contacto> contactos = cliente.get().getContactos();
            contactos.sort(Comparator.comparing(Contacto::getId));
            cliente.get().setContactos(contactos);
            return cliente.get();
        }
        return new Cliente();
    }

    public Cliente createClientSede(String idClient, Sede newSede) {
        Optional<Cliente> cliente = clienteRepository.findById(Long.valueOf(idClient));
        if (cliente.isPresent()) {
            newSede.setCliente(cliente.get());
            Sede sede = sedeRepository.save(newSede);
            cliente.get().addSede(sede);
            return clienteRepository.save(cliente.get());
        }
        return new Cliente();
    }

    @Override
    public Cliente updateClientBasicInfo(Long idClient, String nit, String razonSocial, String correo) {
        Optional<Cliente> cliente = clienteRepository.findById(idClient);
        if (cliente.isPresent()) {
            Cliente clienteToUpdate = cliente.get();
            clienteToUpdate.setNit(nit);
            clienteToUpdate.setRazonSocial(razonSocial);
            clienteToUpdate.setCorreo(correo);
            clienteRepository.save(clienteToUpdate);
            return clienteToUpdate;
        }
        return new Cliente();
    }

    @Override
    public Cliente updateClientContact(Long idClient, Long idContact, Contacto updatedContact) {
        Optional<Cliente> cliente = Optional.of(new Cliente());
        Optional<Contacto> contactoToUpdate = contactoRepository.findById(idContact);
        if (contactoToUpdate.isPresent()){
            contactoToUpdate.get().setNombreContacto(updatedContact.getNombreContacto());
            contactoToUpdate.get().setCorreo(updatedContact.getCorreo());
            contactoToUpdate.get().setAreaTrabajo(updatedContact.getAreaTrabajo());
            contactoToUpdate.get().setCelular(updatedContact.getCelular());
            contactoToUpdate.get().setTelefono(updatedContact.getTelefono());
            contactoRepository.save(contactoToUpdate.get());
            cliente = clienteRepository.findById(idClient);
            List<Contacto> contactos = cliente.get().getContactos();
            contactos.sort(Comparator.comparing(Contacto::getId));
            cliente.get().setContactos(contactos);
        }
        return cliente.orElseGet(Cliente::new);
    }

    @Override
    public Cliente updateClientSede(Long idClient, Long idSede, Sede updatedSede) {
        Optional<Cliente> cliente = Optional.of(new Cliente());
        Optional<Sede> sedeToUpdate = sedeRepository.findById(idSede);
        if (sedeToUpdate.isPresent()){
            sedeRepository.save(updatedSede);
            cliente = clienteRepository.findById(idClient);
        }
        return cliente.orElseGet(Cliente::new);
    }

    @Override
    public Cliente deleteClientContact(Long idClient, Long idContact) {
        Optional<Cliente> cliente = clienteRepository.findById(idClient);
        if (cliente.isPresent()) {
            Cliente clienteActual = cliente.get();
            clienteActual.setContactos(clienteActual.getContactos().stream()
                    .filter(contacto -> !contacto.getId().equals(idContact)).collect(Collectors.toList()));
            contactoRepository.deleteById(idContact);
            List<Contacto> contactos = clienteActual.getContactos();
            contactos.sort(Comparator.comparing(Contacto::getId));
            clienteActual.setContactos(contactos);
            return clienteActual;
        }
        return new Cliente();
    }

    @Override
    public Cliente deleteClientSede(Long idClient, Long idSede) {
        Optional<Cliente> cliente = clienteRepository.findById(idClient);
        if (cliente.isPresent()) {
            Cliente clienteActual = cliente.get();
            clienteActual.setSedes(clienteActual.getSedes().stream()
                    .filter(sede -> !sede.getId().equals(idSede)).collect(Collectors.toList()));
            sedeRepository.deleteById(idSede);
            return clienteActual;
        }
        return new Cliente();
    }

    public List<ClienteDTO> getClients() {
        List<ClienteDTO> clientesColumnas = new ArrayList<>();
        log.info("Recolectando clientes de Metrilab");
        List<Cliente> clients = clienteRepository.findAll();
        if (clients.size() > 0) {
            log.info("Se han encontrado clientes ");
            clients.forEach(cliente -> {
                String sedes = String.join(", ", getUniqueClientCities(cliente));
                ClienteDTO clienteDTO = new ClienteDTO(cliente.getId(), cliente.getRazonSocial(), cliente.getNit(), cliente.getCorreo(), sedes);

                clientesColumnas.add(clienteDTO);
            });
            log.info("Finalizando recoleccion de clientes, retornando lista");
        }
        return clientesColumnas;
    }

    @Override
    public List<String> getUniqueClientCities(Cliente cliente) {
        return cliente.getSedes().stream().map(Sede::getCiudad).distinct().collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Cliente createClient(String razonSocial, String nit, String email, Sede clientSede) {
        String nitCleared = nit.isEmpty() ? null : nit;
        Cliente cliente = Cliente.builder().razonSocial(razonSocial).nit(nitCleared).correo(email)
                .fechaCreacion(LocalDateTime.now().toLocalDate()).build();

        log.info(clientSede.toString());

        cliente.addSede(clientSede);
        cliente = clienteRepository.save(cliente);

        log.info("Cliente guardado en la base de datos");

        clientSede.setCliente(cliente);
        sedeRepository.save(clientSede);

        return cliente;
    }
}
