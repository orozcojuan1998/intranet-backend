package com.metrilab.intranet.controller;

import com.metrilab.intranet.dto.ClienteDTO;
import com.metrilab.intranet.modelo.Cliente;
import com.metrilab.intranet.modelo.Contacto;
import com.metrilab.intranet.modelo.Sede;
import com.metrilab.intranet.service.ClientesService;
import com.sun.istack.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@Controller
@RequestMapping(value = "/clientes")
public class ClienteController {

    @Autowired
    ClientesService clientesService;

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Cliente> getClientById(@PathVariable @NotNull String id) {
        Cliente cliente = new Cliente();
        try {
            log.info("Trying to get the clients in the system");
            cliente = clientesService.getClienteById(id);
            return ResponseEntity.status(HttpStatus.OK).body(cliente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cliente);
        }
    }

    @GetMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClienteDTO>> getAllClients() {
        try {
            log.info("Trying to get the clients in the system");
            return ResponseEntity.status(HttpStatus.OK).body(clientesService.getClients());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        }
    }

    @GetMapping(path = "/clientHeadquarters", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<String>> getAllClientsSedes() {
        try {
            log.info("Trying to get the clients in the system");
            return ResponseEntity.status(HttpStatus.OK).body(clientesService.getUniqueClientCities(null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        }
    }

    @PostMapping(path = "/new", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ClienteDTO>> createNewClientWithLocations(@RequestBody LinkedHashMap<String, LinkedHashMap<String, Object>> data) {
        try {
            log.info("Trying to insert the client in the system");
            String nombre = String.valueOf(data.get("data").get("razonSocial"));
            String nit = String.valueOf(data.get("data").get("nit")).replaceAll("[,\\s]", "");
            String correo = String.valueOf(data.get("data").get("correo"));
            Sede clientSede = Sede.builder().pais(String.valueOf(data.get("data").get("pais")))
                    .departamento(String.valueOf(data.get("data").get("departamento")))
                    .ciudad((String.valueOf(data.get("data").get("ciudad"))))
                    .direccion((String.valueOf(data.get("data").get("direccion"))))
                    .build();

            clientesService.createClient(nombre, nit, correo, clientSede);
            return ResponseEntity.status(HttpStatus.CREATED).body(clientesService.getClients());
        } catch (Exception e) {
            log.info(e.toString());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/addClientContact", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Cliente> createNewClientContact(@RequestBody LinkedHashMap<String, LinkedHashMap<String, Object>> data) {
        Cliente cliente = new Cliente();
        try {
            log.info("Agregar nuevo contacto al cliente");
            String clientId = String.valueOf(data.get("data").get("id"));
            LinkedHashMap<String, String> contacto = (LinkedHashMap<String, String>) data.get("data").get("contacto");
            Contacto contactoCliente = Contacto.builder().nombreContacto(contacto.get("nombreContacto"))
                    .areaTrabajo(contacto.get("areaTrabajo")).correo(contacto.get("correo"))
                    .correoAlternativo(contacto.get("correoAlternativo"))
                    .telefono(contacto.get("telefono")).celular(contacto.get("celular")).build();
            String sedeId = contacto.get("sede");
            cliente = clientesService.createClientContact(clientId, contactoCliente, sedeId);
            return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
        } catch (Exception e) {
            log.info(e.toString());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cliente);
        }
    }

    @SuppressWarnings("unchecked")
    @PostMapping(path = "/addClientSede", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Cliente> createNewClientSede(@RequestBody LinkedHashMap<String, LinkedHashMap<String, Object>> data) {
        Cliente cliente = new Cliente();
        try {
            log.info("Trying to insert the client sede in the system");
            String clientId = String.valueOf(data.get("data").get("id"));
            LinkedHashMap<String, String> sede = (LinkedHashMap<String, String>) data.get("data").get("sede");
            Sede sedeCliente = Sede.builder().pais(sede.get("pais"))
                    .departamento(sede.get("departamento")).ciudad(sede.get("ciudad"))
                    .direccion(sede.get("direccion")).build();
            cliente = clientesService.createClientSede(clientId, sedeCliente);
            return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
        } catch (Exception e) {
            log.info(e.toString());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cliente);
        }
    }

    @PutMapping(path = "/updateClient", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Cliente> updateExistingClient(@RequestBody LinkedHashMap<String, LinkedHashMap<String, Object>> data) {
        Cliente cliente = new Cliente();
        try {
            log.info("Trying to update the client in the system");
            Integer clientId = (Integer) data.get("data").get("idClient");
            String nit = String.valueOf(data.get("data").get("nit")).replaceAll("[,\\s]", "");
            String razonSocial = String.valueOf(data.get("data").get("razonSocial"));
            String correo = String.valueOf(data.get("data").get("correo"));

            cliente = clientesService.updateClientBasicInfo(Long.valueOf(clientId), nit, razonSocial, correo);
            return ResponseEntity.status(HttpStatus.OK).body(cliente);
        } catch (Exception e) {
            log.info(e.toString());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cliente);
        }
    }

    @SuppressWarnings("unchecked")
    @PutMapping(path = "/editClientContact", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Cliente> updateExistingClientContact(@RequestBody LinkedHashMap<String, LinkedHashMap<String, Object>> data) {
        Cliente cliente = new Cliente();
        try {
            log.info("Edición del cliente en el sistema para el cliente enviado");
            Long clientId = Long.valueOf(String.valueOf(data.get("data").get("id")));
            Long contactId = Long.valueOf(String.valueOf(data.get("data").get("contactId")));
            LinkedHashMap<String, String> contacto = (LinkedHashMap<String, String>) data.get("data").get("contacto");
            Contacto contactoCliente = Contacto.builder().nombreContacto(contacto.get("nombreContacto"))
                    .areaTrabajo(contacto.get("areaTrabajo")).correo(contacto.get("correo"))
                    .correoAlternativo(contacto.get("correoAlternativo"))
                    .telefono(contacto.get("telefono")).celular(contacto.get("celular")).build();

            cliente = clientesService.updateClientContact(clientId, contactId, contactoCliente);
            return ResponseEntity.status(HttpStatus.OK).body(cliente);
        } catch (Exception e) {
            log.info(e.toString());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cliente);
        }
    }

    @SuppressWarnings("unchecked")
    @PutMapping(path = "/editClientSede", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Cliente> updateExistingClientSede(@RequestBody LinkedHashMap<String, LinkedHashMap<String, Object>> data) {
        Cliente cliente = new Cliente();
        try {
            log.info("Editar sede del cliete enviado");
            String clientId = String.valueOf(data.get("data").get("id"));
            String sedeId = String.valueOf(data.get("data").get("sedeId"));
            LinkedHashMap<String, String> sede = (LinkedHashMap<String, String>) data.get("data").get("sede");
            Sede sedeCliente = Sede.builder().id(Long.valueOf(sedeId)).pais(sede.get("pais"))
                    .departamento(sede.get("departamento")).ciudad(sede.get("ciudad"))
                    .direccion(sede.get("direccion")).build();
            cliente = clientesService.createClientSede(clientId, sedeCliente);

            cliente = clientesService.updateClientSede(Long.valueOf(clientId), Long.valueOf(sedeId), sedeCliente);
            return ResponseEntity.status(HttpStatus.OK).body(cliente);
        } catch (Exception e) {
            log.info(e.toString());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cliente);
        }
    }

    @DeleteMapping(path = "/deleteClientContact", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Cliente> deleteClientContact(@RequestBody LinkedHashMap<String, Long> data) {
        Cliente cliente = null;
        try {
            log.info("Trying to delete the client contact in the system");
            cliente = clientesService.deleteClientContact(data.get("idClient"), data.get("idContact"));
            return ResponseEntity.status(HttpStatus.OK).body(cliente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cliente);
        }
    }

    @DeleteMapping(path = "/deleteClientSede", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Cliente> deleteClientSede(@RequestBody LinkedHashMap<String, Long> data) {
        Cliente cliente = null;
        try {
            log.info("Trying to delete the client sede in the system");
            cliente = clientesService.deleteClientSede(data.get("idClient"), data.get("idSede"));
            return ResponseEntity.status(HttpStatus.OK).body(cliente);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(cliente);
        }
    }

}
