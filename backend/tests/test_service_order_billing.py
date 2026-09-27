from app.models import Role
from tests.conftest import login_headers
from tests.test_inventory import PART, create_part


async def create_order(api, headers, client, vehicle, **extra):
    resp = await api.post(
        "/service-orders",
        json={"clientId": str(client.id), "vehicleId": str(vehicle.id), **extra},
        headers=headers,
    )
    assert resp.status_code == 201, resp.text
    return resp.json()


async def test_adding_item_bills_it_and_debits_stock(api, admin, client_and_vehicle):
    client, vehicle = client_and_vehicle
    part = await create_part(api, admin)  # salePrice 25.9, stockQuantity 10
    order = await create_order(api, admin, client, vehicle)

    resp = await api.post(
        f"/service-orders/{order['id']}/items",
        json={"partId": part["id"], "quantity": 2},
        headers=admin,
    )
    assert resp.status_code == 201, resp.text
    updated = resp.json()
    assert len(updated["items"]) == 1
    assert updated["items"][0]["unitPrice"] == 25.9
    assert updated["partsAmount"] == 51.8
    assert updated["totalAmount"] == 51.8

    part_after = (await api.get(f"/inventory/parts/{part['id']}", headers=admin)).json()
    assert part_after["stockQuantity"] == 8


async def test_removing_item_restocks_and_recalculates_total(api, admin, client_and_vehicle):
    client, vehicle = client_and_vehicle
    part = await create_part(api, admin)
    order = await create_order(api, admin, client, vehicle)
    added = (
        await api.post(
            f"/service-orders/{order['id']}/items",
            json={"partId": part["id"], "quantity": 3},
            headers=admin,
        )
    ).json()
    item_id = added["items"][0]["id"]

    resp = await api.delete(f"/service-orders/{order['id']}/items/{item_id}", headers=admin)
    assert resp.status_code == 200, resp.text
    assert resp.json()["items"] == []
    assert resp.json()["totalAmount"] == 0

    part_after = (await api.get(f"/inventory/parts/{part['id']}", headers=admin)).json()
    assert part_after["stockQuantity"] == PART["stockQuantity"]


async def test_insufficient_stock_is_rejected(api, admin, client_and_vehicle):
    client, vehicle = client_and_vehicle
    part = await create_part(api, admin, stockQuantity=1)
    order = await create_order(api, admin, client, vehicle)

    resp = await api.post(
        f"/service-orders/{order['id']}/items",
        json={"partId": part["id"], "quantity": 5},
        headers=admin,
    )
    assert resp.status_code == 400
    assert "Estoque insuficiente" in resp.json()["message"]


async def test_labor_amount_adds_to_total(api, admin, client_and_vehicle):
    client, vehicle = client_and_vehicle
    part = await create_part(api, admin)
    order = await create_order(api, admin, client, vehicle)
    await api.post(
        f"/service-orders/{order['id']}/items",
        json={"partId": part["id"], "quantity": 1},
        headers=admin,
    )

    resp = await api.patch(
        f"/service-orders/{order['id']}/labor",
        json={"laborAmount": 150},
        headers=admin,
    )
    assert resp.status_code == 200, resp.text
    body = resp.json()
    assert body["laborAmount"] == 150.0
    assert body["partsAmount"] == 25.9
    assert body["totalAmount"] == 175.9


async def test_billing_is_frozen_after_order_is_closed(api, admin, client_and_vehicle):
    client, vehicle = client_and_vehicle
    part = await create_part(api, admin)
    order = await create_order(api, admin, client, vehicle)
    await api.patch(
        f"/service-orders/{order['id']}/status", json={"status": "CANCELADA"}, headers=admin
    )

    resp = await api.post(
        f"/service-orders/{order['id']}/items",
        json={"partId": part["id"], "quantity": 1},
        headers=admin,
    )
    assert resp.status_code == 400
    assert "finalizada ou cancelada" in resp.json()["message"]


async def test_mechanic_can_only_bill_their_own_order(
    api, admin, client_and_vehicle, mechanic_profile
):
    client, vehicle = client_and_vehicle
    user, mechanic = mechanic_profile
    part = await create_part(api, admin)
    mine = await create_order(api, admin, client, vehicle, mechanicId=str(mechanic.id))
    someone_elses = await create_order(api, admin, client, vehicle)

    headers = await login_headers(api, user)
    ok = await api.post(
        f"/service-orders/{mine['id']}/items",
        json={"partId": part["id"], "quantity": 1},
        headers=headers,
    )
    assert ok.status_code == 201, ok.text

    blocked = await api.post(
        f"/service-orders/{someone_elses['id']}/items",
        json={"partId": part["id"], "quantity": 1},
        headers=headers,
    )
    assert blocked.status_code == 403


async def test_receptionist_cannot_touch_status_but_can_bill(
    api, admin, client_and_vehicle, headers_for
):
    """RECEPTIONIST não muda status (fora do escopo dela), mas lança peças/mão de obra."""
    client, vehicle = client_and_vehicle
    part = await create_part(api, admin)
    order = await create_order(api, admin, client, vehicle)
    headers = await headers_for(Role.RECEPTIONIST)

    resp = await api.post(
        f"/service-orders/{order['id']}/items",
        json={"partId": part["id"], "quantity": 1},
        headers=headers,
    )
    assert resp.status_code == 201, resp.text
