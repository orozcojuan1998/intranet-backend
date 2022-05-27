package com.metrilab.intranet.service;

import com.metrilab.intranet.dto.ClienteDTO;
import com.metrilab.intranet.modelo.Cliente;
import com.metrilab.intranet.modelo.Contacto;
import com.metrilab.intranet.modelo.Sede;

import java.util.LinkedHashMap;
import java.util.List;

public interface ClientesService {

    Cliente createClient(String razonSocial, String nit, String email, Sede clientSede);

    Cliente getClienteById(String id);

    Cliente createClientContact(String idClient, Contacto contact, String sedeId);

    Cliente createClientSede(String idClient, Sede sede);

    Cliente updateClientBasicInfo(Long idClient, String nit, String razonSocial, String correo);

    Cliente updateClientContact(Long idClient, Long idContact, Contacto updatedContact);

    Cliente updateClientSede(Long idClient, Long idSede, Sede updatedSede);

    Cliente deleteClientContact(Long idClient, Long idContact);

    Cliente deleteClientSede(Long idClient, Long idSede);

    List<ClienteDTO> getClients();

    List<String> getUniqueClientCities(Cliente cliente);
}
