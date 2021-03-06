import React from 'react';
import ClocksList, {PAUSED_BTN, RESUME_BTN} from "../clocks_list";
import {cleanup, render, wait, fireEvent} from "react-testing-library";
import "jest-dom/extend-expect"
import axios from 'axios';
import MockAdapter from "axios-mock-adapter";
import {createAppStore} from "../../../store";
import {ACTIVE, PAUSED} from "../actions";

let store;
const backend = new MockAdapter(axios);
const apiKey = '121234';

beforeEach(() => {
  backend.reset();
  store = createAppStore();
});

test("Show no clocks message", async () => {
  const {queryByText} = await givenTheClocks([]);
  await wait(() => expect(queryByText(ClocksList.NO_CLOCKS_MSG)).toBeInTheDocument());
});

const givenTheClocks = async (clocks) => {
  backend.onGet(`/api/v1/clocks?access_token=${apiKey}`).reply(200, clocks);
  const renderResult = render(<ClocksList store={store} apiKey={apiKey}/>);
  if (clocks.length > 0) {
    await wait(() => expect(renderResult.queryByText(ClocksList.NO_CLOCKS_MSG)).not.toBeInTheDocument());
  }
  return renderResult;
};

test("Show fetched clocks", async () => {
  let clocks = [
    {id: '11', name: '3232', schedule: "every.2.seconds"},
    {id: '22', name: '1212', schedule: "every.8.seconds"}
  ];
  const rows = await getClockRowsFor(clocks);
  expect(rows).toHaveLength(2);
  expect(rows[0]).toHaveTextContent(clocks[0].schedule);
  expect(rows[1]).toHaveTextContent(clocks[1].schedule);
});

let getClockRowsFor = async function (clocks) {
  const {getAllByTestId} = await givenTheClocks(clocks);
  return getAllByTestId("clock-row");
};

test("Show paused clocks", async () => {
  let clocks = [
    {id: '1', name: '3232', schedule: "every.2.seconds", status: PAUSED},
    {id: '2', name: '3232', schedule: "every.2.seconds", status: ACTIVE}
  ];
  const rows = await getClockRowsFor(clocks);
  expect(rows[0].cells[2]).toHaveTextContent(RESUME_BTN);
  expect(rows[1].cells[2]).toHaveTextContent(PAUSED_BTN);
});

test("Resume a paused clock", async () => {
  let clocks = [
    {id: '1', name: '3232', schedule: "every.2.seconds", status: PAUSED},
  ];
  backend.onPut(`/api/v1/clocks/1/resume?access_token=${apiKey}`).reply(200);

  const {getAllByTestId} = await givenTheClocks(clocks);

  fireEvent.click(getAllByTestId('clock-row')[0].querySelector('.btn'));
  await wait(() => expect(getAllByTestId('clock-row')[0].querySelector('.btn')).toHaveTextContent(PAUSED_BTN));

  expect(backend.history.put.length).toBe(1);
});


afterEach(cleanup);

afterAll(() => {
  backend.restore()
});
